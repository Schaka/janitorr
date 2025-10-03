package com.github.schaka.janitorr.external.common

data class MediaIntelligence(
    val tmdbRating: Double? = null,        // 0-10
    val imdbRating: Double? = null,        // 0-10
    val popularityScore: Double? = null,   // 0-100
    val trendingScore: Double? = null,     // 0-100
    val availabilityScore: Double? = null, // 0-100 (streaming availability)
    val collectibilityScore: Double? = null, // 0-100 (rarity/collectible value)
    val overallScore: Double = 0.0       // Weighted composite
) {
    companion object {
        fun empty() = MediaIntelligence()
    }
}
