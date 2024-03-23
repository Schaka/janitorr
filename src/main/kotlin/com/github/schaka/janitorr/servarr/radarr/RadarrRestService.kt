package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import com.github.schaka.janitorr.servarr.radarr.movie.MoviePayload
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.exists

@Service
@RegisterReflectionForBinding(classes = [QualityProfile::class, Tag::class, MoviePayload::class, HistoryResponse::class])
class RadarrRestService(

        val radarrClient: RadarrClient,

        val applicationProperties: ApplicationProperties,

        val fileSystemProperties: FileSystemProperties,

        val radarrProperties: RadarrProperties,

        var upgradesAllowed: Boolean = false,

        var keepTag: Tag = Tag(Integer.MIN_VALUE, "Not_Set")

) : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

        const val CACHE_NAME = "radarr-cache"
    }
    init {
        if (radarrProperties.enabled) {
            upgradesAllowed = radarrClient.getAllQualityProfiles().any { it.items.isNotEmpty() && it.upgradeAllowed }
            keepTag = radarrClient.getAllTags().firstOrNull { it.label == applicationProperties.exclusionTag } ?: keepTag
        }
    }

    @Cacheable(CACHE_NAME)
    override fun getEntries(): List<LibraryItem> {
        val allTags = radarrClient.getAllTags()

        return radarrClient.getAllMovies()
                .filter { !it.tags.contains(keepTag.id) }
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
                            .sortedWith(byDate(upgradesAllowed))
                            .firstOrNull()
                }
    }

    override fun removeEntries(items: List<LibraryItem>) {
        for (movie in items) {

            if (fileSystemProperties.access && Path.of(movie.originalPath).exists()) {
                log.info("Can't delete movie [still seeding - file exists] ({}), id: {}, imdb: {}", movie.originalPath, movie.id, movie.imdbId)
                movie.seeding = true
                continue
            }

            if (!applicationProperties.dryRun) {
                unmonitorMovie(movie.id)
                radarrClient.deleteMovie(movie.id)
                log.info("Deleting movie ({}), id: {}, imdb: {}", movie.parentPath, movie.id, movie.imdbId)
            } else {
                log.info("Deleting movie ({}), id: {}, imdb: {}", movie.parentPath, movie.id, movie.imdbId)
            }
        }
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