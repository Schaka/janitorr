package com.github.schaka.janitorr.ai.data

/**
 * Feature vector for a media item used in ML models.
 * Contains various metrics and signals used to predict cleanup decisions.
 */
data class MediaFeatures(
    /**
     * Average number of times content is watched per week
     */
    val watchFrequency: Double,
    
    /**
     * Number of days since last viewing
     */
    val daysSinceLastWatch: Int,
    
    /**
     * Average percentage of content watched when played (0.0 to 1.0)
     */
    val avgCompletionRate: Double,
    
    /**
     * User's preference score for this genre (0.0 to 1.0)
     */
    val genreScore: Double,
    
    /**
     * External popularity/trending score (0.0 to 1.0)
     */
    val trendingScore: Double,
    
    /**
     * Storage impact in GB
     */
    val storageImpact: Double,
    
    /**
     * Relevance based on seasonal patterns (0.0 to 1.0)
     */
    val seasonalRelevance: Double,
    
    /**
     * How similar content has been treated (0.0 to 1.0)
     */
    val similarContentBehavior: Double,
    
    /**
     * Additional custom features
     */
    val customFeatures: Map<String, Double> = emptyMap()
) {
    /**
     * Convert features to a map for ML processing
     */
    fun toMap(): Map<String, Double> {
        return mapOf(
            "watch_frequency" to watchFrequency,
            "days_since_last_watch" to daysSinceLastWatch.toDouble(),
            "avg_completion_rate" to avgCompletionRate,
            "genre_score" to genreScore,
            "trending_score" to trendingScore,
            "storage_impact" to storageImpact,
            "seasonal_relevance" to seasonalRelevance,
            "similar_content_behavior" to similarContentBehavior
        ) + customFeatures
    }
    
    companion object {
        /**
         * Create MediaFeatures from a map (useful for model input)
         */
        fun fromMap(features: Map<String, Double>): MediaFeatures {
            return MediaFeatures(
                watchFrequency = features["watch_frequency"] ?: 0.0,
                daysSinceLastWatch = features["days_since_last_watch"]?.toInt() ?: 0,
                avgCompletionRate = features["avg_completion_rate"] ?: 0.0,
                genreScore = features["genre_score"] ?: 0.0,
                trendingScore = features["trending_score"] ?: 0.0,
                storageImpact = features["storage_impact"] ?: 0.0,
                seasonalRelevance = features["seasonal_relevance"] ?: 0.0,
                similarContentBehavior = features["similar_content_behavior"] ?: 0.0,
                customFeatures = features.filterKeys { 
                    it !in setOf(
                        "watch_frequency", "days_since_last_watch", "avg_completion_rate",
                        "genre_score", "trending_score", "storage_impact",
                        "seasonal_relevance", "similar_content_behavior"
                    )
                }
            )
        }
    }
}
