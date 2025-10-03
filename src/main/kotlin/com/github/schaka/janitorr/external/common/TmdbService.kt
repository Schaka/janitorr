package com.github.schaka.janitorr.external.common

import com.github.schaka.janitorr.external.tmdb.TmdbClient
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "external-apis.tmdb", name = ["enabled"], havingValue = "true")
class TmdbService(
    private val tmdbClient: TmdbClient,
    private val externalDataProperties: ExternalDataProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    private val apiKey get() = externalDataProperties.tmdb.apiKey

    fun getRating(item: LibraryItem): Double? {
        return try {
            item.tmdbId?.let { tmdbId ->
                if (item.season != null) {
                    val response = tmdbClient.getTvShow(tmdbId, apiKey)
                    response.vote_average
                } else {
                    val response = tmdbClient.getMovie(tmdbId, apiKey)
                    response.vote_average
                }
            }
        } catch (e: Exception) {
            log.debug("Failed to get TMDB rating for item: ${item.filePath}", e)
            null
        }
    }

    fun getPopularityScore(item: LibraryItem): Double? {
        return try {
            item.tmdbId?.let { tmdbId ->
                val popularity = if (item.season != null) {
                    val response = tmdbClient.getTvShow(tmdbId, apiKey)
                    response.popularity
                } else {
                    val response = tmdbClient.getMovie(tmdbId, apiKey)
                    response.popularity
                }
                // Normalize popularity to 0-100 scale (TMDB popularity can vary widely)
                normalizePopularity(popularity)
            }
        } catch (e: Exception) {
            log.debug("Failed to get TMDB popularity for item: ${item.filePath}", e)
            null
        }
    }

    fun isTrending(item: LibraryItem): Boolean {
        return try {
            item.tmdbId?.let { tmdbId ->
                val mediaType = if (item.season != null) "tv" else "movie"
                val trendingDaily = tmdbClient.getTrending(mediaType, "day", apiKey)
                trendingDaily.results.any { it.id == tmdbId }
            } ?: false
        } catch (e: Exception) {
            log.debug("Failed to check TMDB trending status for item: ${item.filePath}", e)
            false
        }
    }

    fun isPartOfCollection(item: LibraryItem): Boolean {
        return try {
            item.tmdbId?.let { tmdbId ->
                if (item.season != null) {
                    false // TV shows don't have collections in the same way
                } else {
                    val response = tmdbClient.getMovie(tmdbId, apiKey)
                    response.belongs_to_collection != null
                }
            } ?: false
        } catch (e: Exception) {
            log.debug("Failed to check TMDB collection status for item: ${item.filePath}", e)
            false
        }
    }

    private fun normalizePopularity(popularity: Double): Double {
        // TMDB popularity typically ranges from 0 to ~1000 for popular content
        // Normalize to 0-100 scale using a logarithmic approach
        return when {
            popularity >= 1000 -> 100.0
            popularity >= 500 -> 90.0
            popularity >= 100 -> 75.0
            popularity >= 50 -> 60.0
            popularity >= 25 -> 45.0
            popularity >= 10 -> 30.0
            popularity >= 5 -> 20.0
            else -> popularity * 2 // For very low popularity
        }.coerceIn(0.0, 100.0)
    }
}
