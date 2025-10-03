package com.github.schaka.janitorr.ai.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Test to ensure that AI features are disabled by default
 * and configuration properties work as expected.
 */
internal class AIPropertiesTest {

    @Test
    fun testAIDisabledByDefault() {
        val aiProperties = AIProperties()
        assertFalse(aiProperties.enabled, "AI features should be disabled by default")
    }

    @Test
    fun testAICanBeEnabled() {
        val aiProperties = AIProperties(enabled = true)
        assertEquals(true, aiProperties.enabled, "AI features should be enabled when explicitly set")
    }

    @Test
    fun testTrainingDisabledByDefault() {
        val trainingProperties = TrainingProperties()
        assertFalse(trainingProperties.enabled, "AI training should be disabled by default")
    }

    @Test
    fun testTrainingScheduleDefault() {
        val trainingProperties = TrainingProperties()
        assertEquals("0 0 3 * * ?", trainingProperties.schedule, "Training schedule should default to 3 AM daily")
    }

    @Test
    fun testMinDataPointsDefault() {
        val trainingProperties = TrainingProperties()
        assertEquals(1000, trainingProperties.minDataPoints, "Minimum data points should default to 1000")
    }

    @Test
    fun testHistoricalDataDaysDefault() {
        val trainingProperties = TrainingProperties()
        assertEquals(90, trainingProperties.historicalDataDays, "Historical data days should default to 90")
    }

    @Test
    fun testInferenceCacheTtlDefault() {
        val inferenceProperties = InferenceProperties()
        assertEquals(3600, inferenceProperties.cacheTtl, "Cache TTL should default to 3600 seconds (1 hour)")
    }

    @Test
    fun testInferenceBatchSizeDefault() {
        val inferenceProperties = InferenceProperties()
        assertEquals(100, inferenceProperties.batchSize, "Batch size should default to 100")
    }

    @Test
    fun testInferenceConfidenceThresholdDefault() {
        val inferenceProperties = InferenceProperties()
        assertEquals(0.7, inferenceProperties.confidenceThreshold, "Confidence threshold should default to 0.7")
    }

    @Test
    fun testInferenceTimeoutDefault() {
        val inferenceProperties = InferenceProperties()
        assertEquals(100L, inferenceProperties.timeoutMs, "Timeout should default to 100ms")
    }

    @Test
    fun testExternalApisDisabledByDefault() {
        val featureProperties = FeatureProperties()
        assertFalse(featureProperties.externalApis, "External APIs should be disabled by default for privacy")
    }

    @Test
    fun testUserFeedbackEnabledByDefault() {
        val featureProperties = FeatureProperties()
        assertEquals(true, featureProperties.userFeedback, "User feedback should be enabled by default")
    }

    @Test
    fun testNaturalLanguageDisabledByDefault() {
        val featureProperties = FeatureProperties()
        assertFalse(featureProperties.naturalLanguage, "Natural language features should be disabled by default")
    }

    @Test
    fun testComputerVisionDisabledByDefault() {
        val featureProperties = FeatureProperties()
        assertFalse(featureProperties.computerVision, "Computer vision features should be disabled by default")
    }

    @Test
    fun testModelPathDefault() {
        val aiProperties = AIProperties()
        assertEquals("/config/models", aiProperties.modelPath, "Model path should default to /config/models")
    }

    @Test
    fun testAIPropertiesCanBeCustomized() {
        val customTraining = TrainingProperties(
            enabled = true,
            schedule = "0 0 2 * * ?",
            minDataPoints = 500,
            historicalDataDays = 60
        )
        
        val customInference = InferenceProperties(
            cacheTtl = 7200,
            batchSize = 50,
            confidenceThreshold = 0.8,
            timeoutMs = 200L
        )
        
        val customFeatures = FeatureProperties(
            externalApis = true,
            userFeedback = false,
            naturalLanguage = true,
            computerVision = true
        )
        
        val aiProperties = AIProperties(
            enabled = true,
            modelPath = "/custom/path",
            training = customTraining,
            inference = customInference,
            features = customFeatures
        )
        
        assertEquals(true, aiProperties.enabled)
        assertEquals("/custom/path", aiProperties.modelPath)
        assertEquals(true, aiProperties.training.enabled)
        assertEquals("0 0 2 * * ?", aiProperties.training.schedule)
        assertEquals(500, aiProperties.training.minDataPoints)
        assertEquals(60, aiProperties.training.historicalDataDays)
        assertEquals(7200, aiProperties.inference.cacheTtl)
        assertEquals(50, aiProperties.inference.batchSize)
        assertEquals(0.8, aiProperties.inference.confidenceThreshold)
        assertEquals(200L, aiProperties.inference.timeoutMs)
        assertEquals(true, aiProperties.features.externalApis)
        assertEquals(false, aiProperties.features.userFeedback)
        assertEquals(true, aiProperties.features.naturalLanguage)
        assertEquals(true, aiProperties.features.computerVision)
    }
}
