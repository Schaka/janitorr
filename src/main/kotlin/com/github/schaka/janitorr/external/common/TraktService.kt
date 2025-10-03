package com.github.schaka.janitorr.external.common

import com.github.schaka.janitorr.external.trakt.TraktClient
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "external-apis.trakt", name = ["enabled"], havingValue = "true")
class TraktService(
    private val traktClient: TraktClient,
    private val externalDataProperties: ExternalDataProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    fun getWatcherCount(item: LibraryItem): Int {
        return try {
            val id = item.imdbId ?: item.tmdbId?.toString() ?: return 0
            val stats = if (item.season != null) {
                traktClient.getShowStats(id)
            } else {
                traktClient.getMovieStats(id)
            }
            stats.watchers
        } catch (e: Exception) {
            log.debug("Failed to get Trakt watcher count for item: ${item.filePath}", e)
            0
        }
    }

    fun getCollectorCount(item: LibraryItem): Int {
        return try {
            val id = item.imdbId ?: item.tmdbId?.toString() ?: return 0
            val stats = if (item.season != null) {
                traktClient.getShowStats(id)
            } else {
                traktClient.getMovieStats(id)
            }
            stats.collectors
        } catch (e: Exception) {
            log.debug("Failed to get Trakt collector count for item: ${item.filePath}", e)
            0
        }
    }

    fun isTrending(item: LibraryItem): Boolean {
        return try {
            val trending = if (item.season != null) {
                traktClient.getTrendingShows()
            } else {
                traktClient.getTrendingMovies()
            }

            trending.any { trendingItem ->
                when {
                    trendingItem.movie != null -> {
                        trendingItem.movie.ids.imdb == item.imdbId ||
                                trendingItem.movie.ids.tmdb == item.tmdbId
                    }
                    trendingItem.show != null -> {
                        trendingItem.show.ids.imdb == item.imdbId ||
                                trendingItem.show.ids.tmdb == item.tmdbId
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            log.debug("Failed to check Trakt trending status for item: ${item.filePath}", e)
            false
        }
    }
}
