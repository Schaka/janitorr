package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.HistorySort
import com.github.schaka.janitorr.servarr.HistorySort.MOST_RECENT
import com.github.schaka.janitorr.servarr.HistorySort.OLDEST
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.data_structures.SonarrImportListExclusion
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.radarr.movie.MovieFile
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.io.path.exists

@Service
class RadarrRestService(

    val radarrClient: RadarrClient,

    val applicationProperties: ApplicationProperties,

    val fileSystemProperties: FileSystemProperties,

    val radarrProperties: RadarrProperties,

    var upgradesAllowed: Boolean = false,

    var keepTags: List<Tag> = listOf(),

    var historySort: HistorySort = OLDEST

) : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

        const val CACHE_NAME = "radarr-cache"
    }
    init {
        if (radarrProperties.enabled && !applicationProperties.trainingRun) {
            upgradesAllowed = radarrClient.getAllQualityProfiles().any { it.items.isNotEmpty() && it.upgradeAllowed }
            historySort = radarrProperties.determineAgeBy ?: if (upgradesAllowed) MOST_RECENT else OLDEST
            keepTags = radarrClient.getAllTags().filter { applicationProperties.exclusionTags.contains(it.label) }
        }
    }

    @Cacheable(CACHE_NAME)
    override fun getEntries(): List<LibraryItem> {
        val allTags = radarrClient.getAllTags()

        return radarrClient.getAllMovies()
            .filter { !it.tags.any { movieTagId -> keepTags.any { kTag -> kTag.id == movieTagId } } }
                .mapNotNull { movie ->
                    radarrClient.getHistory(movie.id)
                            .filter { movie.movieFile != null && it.eventType == "downloadFolderImported" && it.data.droppedPath != null }
                            .map {
                                LibraryItem(
                                        movie.id,
                                        LocalDateTime.parse(it.date.substring(0, it.date.length - 1)),
                                        it.data.droppedPath!!,
                                        it.data.importedPath!!,
                                        movie.path,
                                        movie.rootFolderPath!!,
                                        movie.movieFile!!.path,
                                        tmdbId = movie.tmdbId,
                                        imdbId = movie.imdbId,
                                        tags = allTags.filter { tag -> movie.tags.contains(tag.id) }.map { tag -> tag.label }
                                )
                            }
                            .sortedWith(byDate(historySort))
                            .firstOrNull()
                }
    }

    override fun removeEntries(items: List<LibraryItem>) {
        for (movie in items) {

            if (fileSystemProperties.access && fileSystemProperties.validateSeeding && Path(movie.originalPath).exists()) {
                log.info("Can't delete movie [still seeding - file exists] ({}), id: {}, imdb: {}", movie.originalPath, movie.id, movie.imdbId)
                movie.seeding = true
                continue
            }

            if (!applicationProperties.dryRun) {
                unmonitorMovie(movie.id)
                deleteMovie(movie.id)
                addToExclusionList( movie )
                log.info("Deleting movie ({}), id: {}, imdb: {}", movie.parentPath, movie.id, movie.imdbId)
            } else {
                log.info("Deleting movie ({}), id: {}, imdb: {}", movie.parentPath, movie.id, movie.imdbId)
            }
        }
    }

    private fun addToExclusionList(item: LibraryItem) {
        if (radarrProperties.importExclusions && item.tmdbId != null)  {
            radarrClient.addToImportExclusion(SonarrImportListExclusion("IMDB: ${item.imdbId} by Janitorr", item.tmdbId))
        }
    }

    private fun deleteMovie(movieId: Int) {
        if (!radarrProperties.onlyDeleteFiles) {
            radarrClient.deleteMovie(movieId, true, radarrProperties.importExclusions)
            return
        }

        radarrClient.getMovieFiles(movieId)
            .map(MovieFile::id)
            .forEach(radarrClient::deleteMovieFile)
    }

    private fun unmonitorMovie(movieId: Int) {
        val movie = radarrClient.getMovie(movieId)
        val isMonitored = movie.monitored
        movie.monitored = false
        radarrClient.updateMovie(movieId, movie)

        if (isMonitored) {
            log.info("Unmonitoring {}", movie.title)
        }
    }
}