package com.github.schaka.janitorr.external.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MediaIntelligenceTest {

    @Test
    fun `empty creates MediaIntelligence with zero overall score`() {
        val intelligence = MediaIntelligence.empty()

        assertEquals(0.0, intelligence.overallScore)
        assertNull(intelligence.tmdbRating)
        assertNull(intelligence.imdbRating)
        assertNull(intelligence.popularityScore)
        assertNull(intelligence.trendingScore)
        assertNull(intelligence.availabilityScore)
        assertNull(intelligence.collectibilityScore)
    }

    @Test
    fun `MediaIntelligence can be created with partial data`() {
        val intelligence = MediaIntelligence(
            tmdbRating = 8.5,
            imdbRating = 8.2,
            overallScore = 75.0
        )

        assertEquals(8.5, intelligence.tmdbRating)
        assertEquals(8.2, intelligence.imdbRating)
        assertEquals(75.0, intelligence.overallScore)
        assertNull(intelligence.popularityScore)
        assertNull(intelligence.trendingScore)
    }

    @Test
    fun `MediaIntelligence can be created with all data`() {
        val intelligence = MediaIntelligence(
            tmdbRating = 8.5,
            imdbRating = 8.2,
            popularityScore = 75.0,
            trendingScore = 80.0,
            availabilityScore = 50.0,
            collectibilityScore = 90.0,
            overallScore = 77.0
        )

        assertEquals(8.5, intelligence.tmdbRating)
        assertEquals(8.2, intelligence.imdbRating)
        assertEquals(75.0, intelligence.popularityScore)
        assertEquals(80.0, intelligence.trendingScore)
        assertEquals(50.0, intelligence.availabilityScore)
        assertEquals(90.0, intelligence.collectibilityScore)
        assertEquals(77.0, intelligence.overallScore)
    }
}
