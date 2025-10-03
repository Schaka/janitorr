package com.github.schaka.janitorr.ai.data

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests for MediaFeatures data class and serialization.
 */
internal class MediaFeaturesTest {

    @Test
    fun testMediaFeaturesToMap() {
        val features = MediaFeatures(
            watchFrequency = 2.5,
            daysSinceLastWatch = 30,
            avgCompletionRate = 0.85,
            genreScore = 0.9,
            trendingScore = 0.7,
            storageImpact = 15.5,
            seasonalRelevance = 0.6,
            similarContentBehavior = 0.8
        )
        
        val map = features.toMap()
        
        assertEquals(2.5, map["watch_frequency"])
        assertEquals(30.0, map["days_since_last_watch"])
        assertEquals(0.85, map["avg_completion_rate"])
        assertEquals(0.9, map["genre_score"])
        assertEquals(0.7, map["trending_score"])
        assertEquals(15.5, map["storage_impact"])
        assertEquals(0.6, map["seasonal_relevance"])
        assertEquals(0.8, map["similar_content_behavior"])
    }

    @Test
    fun testMediaFeaturesFromMap() {
        val map = mapOf(
            "watch_frequency" to 2.5,
            "days_since_last_watch" to 30.0,
            "avg_completion_rate" to 0.85,
            "genre_score" to 0.9,
            "trending_score" to 0.7,
            "storage_impact" to 15.5,
            "seasonal_relevance" to 0.6,
            "similar_content_behavior" to 0.8
        )
        
        val features = MediaFeatures.fromMap(map)
        
        assertEquals(2.5, features.watchFrequency)
        assertEquals(30, features.daysSinceLastWatch)
        assertEquals(0.85, features.avgCompletionRate)
        assertEquals(0.9, features.genreScore)
        assertEquals(0.7, features.trendingScore)
        assertEquals(15.5, features.storageImpact)
        assertEquals(0.6, features.seasonalRelevance)
        assertEquals(0.8, features.similarContentBehavior)
    }

    @Test
    fun testMediaFeaturesWithCustomFeatures() {
        val customFeatures = mapOf(
            "custom_feature_1" to 1.5,
            "custom_feature_2" to 2.0
        )
        
        val features = MediaFeatures(
            watchFrequency = 2.5,
            daysSinceLastWatch = 30,
            avgCompletionRate = 0.85,
            genreScore = 0.9,
            trendingScore = 0.7,
            storageImpact = 15.5,
            seasonalRelevance = 0.6,
            similarContentBehavior = 0.8,
            customFeatures = customFeatures
        )
        
        val map = features.toMap()
        
        assertEquals(1.5, map["custom_feature_1"])
        assertEquals(2.0, map["custom_feature_2"])
    }

    @Test
    fun testMediaFeaturesRoundTrip() {
        val original = MediaFeatures(
            watchFrequency = 2.5,
            daysSinceLastWatch = 30,
            avgCompletionRate = 0.85,
            genreScore = 0.9,
            trendingScore = 0.7,
            storageImpact = 15.5,
            seasonalRelevance = 0.6,
            similarContentBehavior = 0.8,
            customFeatures = mapOf("extra" to 3.0)
        )
        
        val map = original.toMap()
        val reconstructed = MediaFeatures.fromMap(map)
        
        assertEquals(original.watchFrequency, reconstructed.watchFrequency)
        assertEquals(original.daysSinceLastWatch, reconstructed.daysSinceLastWatch)
        assertEquals(original.avgCompletionRate, reconstructed.avgCompletionRate)
        assertEquals(original.genreScore, reconstructed.genreScore)
        assertEquals(original.trendingScore, reconstructed.trendingScore)
        assertEquals(original.storageImpact, reconstructed.storageImpact)
        assertEquals(original.seasonalRelevance, reconstructed.seasonalRelevance)
        assertEquals(original.similarContentBehavior, reconstructed.similarContentBehavior)
        assertEquals(original.customFeatures, reconstructed.customFeatures)
    }

    @Test
    fun testMediaFeaturesFromMapWithMissingValues() {
        val map = mapOf<String, Double>()
        
        val features = MediaFeatures.fromMap(map)
        
        // All values should default to 0.0
        assertEquals(0.0, features.watchFrequency)
        assertEquals(0, features.daysSinceLastWatch)
        assertEquals(0.0, features.avgCompletionRate)
        assertEquals(0.0, features.genreScore)
        assertEquals(0.0, features.trendingScore)
        assertEquals(0.0, features.storageImpact)
        assertEquals(0.0, features.seasonalRelevance)
        assertEquals(0.0, features.similarContentBehavior)
    }
}
