package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Service
class RadarrService(

        val radarrClient: RadarrClient,

        val applicationProperties: ApplicationProperties,

        @Radarr
        val client: RestTemplate,

        var upgradesAllowed: Boolean = false

) : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @PostConstruct
    fun postConstruct() {
        upgradesAllowed = radarrClient.getAllQualityProfiles().any { it.items.isNotEmpty() && it.upgradeAllowed }
    }

    override fun getEntries(): List<LibraryItem> {
        return radarrClient.getAllMovies().mapNotNull { movie ->
            radarrClient.getHistory(movie.id)
                    .filter { it.eventType == "downloadFolderImported" && it.data.droppedPath != null }
                    .map {
                        LibraryItem(
                                movie.id,
                                LocalDateTime.parse(it.date.substring(0, it.date.length - 1)),
                                it.data.droppedPath!!,
                                it.data.importedPath!!,
                                movie.path,
                                movie.rootFolderPath!!,
                                movie.folderName!!,
                                tmdbId = movie.tmdbId,
                                imdbId = movie.imdbId
                        )
                    }
                    .sortedWith ( byDate(upgradesAllowed) )
                    .firstOrNull()
        }
    }

    override fun removeEntries(items: List<LibraryItem>) {
        for (movie in items) {
            if (!applicationProperties.dryRun) {
                unmonitorMovie(movie.id)
                radarrClient.deleteMovie(movie.id)
            } else {
                log.info("Deleting movie ({}), id: {}, imdb: {}", movie.parentPath, movie.id, movie.imdbId)
            }
        }
    }

    private fun unmonitorMovie(movieId: Int) {
        val movie = radarrClient.getMovie(movieId)
        movie.monitored = false
        radarrClient.updateMovie(movieId, movie)
    }
}