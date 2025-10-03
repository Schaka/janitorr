package com.github.schaka.janitorr.external.common

import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * Service for enriching media data with external API information and calculating intelligence scores.
 */
@Service
@ConditionalOnProperty(prefix = "external-apis", name = ["enabled"], havingValue = "true")
class ExternalDataService(
    private val externalDataProperties: ExternalDataProperties,
    private val tmdbService: TmdbService? = null,
    private val omdbService: OmdbService? = null,
    private val traktService: TraktService? = null
) : ExternalDataServiceInterface {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        const val CACHE_NAME = "external-data-cache"
    }

    /**
     * Enriches media data with external API information and calculates overall intelligence score.
     */
    @Cacheable(CACHE_NAME, key = "#item.tmdbId ?: (#item.imdbId ?: #item.tvdbId)")
    override fun enrichMediaData(item: LibraryItem): MediaIntelligence {
        if (!externalDataProperties.enabled) {
            return MediaIntelligence.empty()
        }

        try {
            val tmdbRating = tmdbService?.getRating(item)
            val imdbRating = omdbService?.getRating(item)
            val popularityScore = tmdbService?.getPopularityScore(item)
            val trendingScore = getTrendingScore(item)
            val collectibilityScore = calculateCollectibilityScore(item)

            val overallScore = calculateOverallScore(
                tmdbRating = tmdbRating,
                imdbRating = imdbRating,
                popularityScore = popularityScore,
                trendingScore = trendingScore,
                collectibilityScore = collectibilityScore
            )

            return MediaIntelligence(
                tmdbRating = tmdbRating,
                imdbRating = imdbRating,
                popularityScore = popularityScore,
                trendingScore = trendingScore,
                availabilityScore = null, // Future enhancement
                collectibilityScore = collectibilityScore,
                overallScore = overallScore
            )
        } catch (e: Exception) {
            log.warn("Failed to enrich media data for item: ${item.filePath}", e)
            return MediaIntelligence.empty()
        }
    }

    private fun getTrendingScore(item: LibraryItem): Double? {
        return try {
            val tmdbTrending = tmdbService?.isTrending(item) ?: false
            val traktWatchers = traktService?.getWatcherCount(item) ?: 0

            when {
                tmdbTrending && traktWatchers > 1000 -> 100.0
                tmdbTrending || traktWatchers > 500 -> 75.0
                traktWatchers > 100 -> 50.0
                traktWatchers > 10 -> 25.0
                else -> 0.0
            }
        } catch (e: Exception) {
            log.debug("Failed to get trending score for item: ${item.filePath}", e)
            null
        }
    }

    private fun calculateCollectibilityScore(item: LibraryItem): Double? {
        return try {
            val traktCollectors = traktService?.getCollectorCount(item) ?: 0
            val tmdbInCollection = tmdbService?.isPartOfCollection(item) ?: false

            when {
                tmdbInCollection && traktCollectors > 10000 -> 100.0
                tmdbInCollection && traktCollectors > 1000 -> 80.0
                tmdbInCollection -> 60.0
                traktCollectors > 5000 -> 70.0
                traktCollectors > 1000 -> 50.0
                traktCollectors > 100 -> 30.0
                else -> 10.0
            }
        } catch (e: Exception) {
            log.debug("Failed to calculate collectibility score for item: ${item.filePath}", e)
            null
        }
    }

    private fun calculateOverallScore(
        tmdbRating: Double?,
        imdbRating: Double?,
        popularityScore: Double?,
        trendingScore: Double?,
        collectibilityScore: Double?
    ): Double {
        val weights = externalDataProperties.scoring
        var totalScore = 0.0
        var totalWeight = 0.0

        tmdbRating?.let {
            totalScore += (it * 10) * weights.tmdbRatingWeight
            totalWeight += weights.tmdbRatingWeight
        }

        imdbRating?.let {
            totalScore += (it * 10) * weights.imdbRatingWeight
            totalWeight += weights.imdbRatingWeight
        }

        popularityScore?.let {
            totalScore += it * weights.popularityWeight
            totalWeight += weights.popularityWeight
        }

        trendingScore?.let {
            totalScore += it * weights.trendingWeight
            totalWeight += weights.trendingWeight
        }

        collectibilityScore?.let {
            totalScore += it * weights.collectibilityWeight
            totalWeight += weights.collectibilityWeight
        }

        return if (totalWeight > 0) totalScore / totalWeight else 0.0
    }

    /**
     * Determines if media should be protected from deletion based on intelligence score.
     */
    override fun shouldPreserveMedia(intelligence: MediaIntelligence): Boolean {
        // Never delete content with high IMDb rating
        if ((intelligence.imdbRating ?: 0.0) >= 8.0) {
            return true
        }

        // Never delete content with high TMDB rating
        if ((intelligence.tmdbRating ?: 0.0) >= 8.0) {
            return true
        }

        // Keep trending content
        if ((intelligence.trendingScore ?: 0.0) >= 75.0) {
            return true
        }

        // Preserve rare/collectible content
        if ((intelligence.collectibilityScore ?: 0.0) >= 80.0) {
            return true
        }

        // Keep content with high overall score
        return intelligence.overallScore >= 70.0
    }
}
