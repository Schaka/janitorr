package com.github.schaka.janitorr.ai.inference

import com.github.schaka.janitorr.ai.data.Decision
import com.github.schaka.janitorr.ai.data.MediaFeatures

/**
 * AI Recommendation for a media item.
 * Provides actionable suggestions with confidence scores and explanations.
 */
data class AIRecommendation(
    /**
     * Recommended action for the media item
     */
    val action: Decision,
    
    /**
     * Confidence level of the recommendation (0.0 to 1.0)
     */
    val confidence: Double,
    
    /**
     * Human-readable explanation of why this recommendation was made
     */
    val reasoning: String,
    
    /**
     * Feature importance - which factors most influenced this decision
     */
    val featureImportance: Map<String, Double>,
    
    /**
     * Alternative scenarios (e.g., "If you watch this in the next 7 days, recommendation changes to KEEP")
     */
    val alternativeScenarios: List<Scenario> = emptyList()
)

/**
 * Confidence level categories for recommendations
 */
enum class ConfidenceLevel {
    HIGH,       // > 0.85
    MEDIUM,     // 0.70 - 0.85
    LOW         // < 0.70
}

/**
 * Alternative scenario showing how different conditions affect recommendations
 */
data class Scenario(
    val condition: String,
    val resultingAction: Decision,
    val confidence: Double
)

/**
 * Inference engine for making AI-powered predictions.
 * 
 * This is a placeholder interface for future AI/ML implementation.
 * When AI features are disabled, this service should not be used.
 */
interface InferenceEngine {
    
    /**
     * Get AI recommendation for a single media item
     * 
     * @param mediaId Unique identifier of the media
     * @return AI recommendation with confidence and explanation
     */
    fun getRecommendation(mediaId: String): AIRecommendation
    
    /**
     * Batch process multiple library items
     * Useful for overnight scoring of entire library
     * 
     * @param mediaIds List of media identifiers to score
     * @return Map of media IDs to their recommendations
     */
    suspend fun batchScore(mediaIds: List<String>): Map<String, AIRecommendation>
    
    /**
     * Get confidence level for a media item
     * 
     * @param mediaId Unique identifier of the media
     * @return Confidence level category
     */
    fun getConfidence(mediaId: String): ConfidenceLevel
    
    /**
     * Check if the inference engine is ready to make predictions
     * 
     * @return true if models are loaded and engine is operational
     */
    fun isReady(): Boolean
}

/**
 * No-op implementation of InferenceEngine for when AI features are disabled.
 * All methods throw UnsupportedOperationException.
 */
class DisabledInferenceEngine : InferenceEngine {
    
    override fun getRecommendation(mediaId: String): AIRecommendation {
        throw UnsupportedOperationException("AI features are disabled. Enable in configuration to use this feature.")
    }
    
    override suspend fun batchScore(mediaIds: List<String>): Map<String, AIRecommendation> {
        throw UnsupportedOperationException("AI features are disabled. Enable in configuration to use this feature.")
    }
    
    override fun getConfidence(mediaId: String): ConfidenceLevel {
        throw UnsupportedOperationException("AI features are disabled. Enable in configuration to use this feature.")
    }
    
    override fun isReady(): Boolean = false
}
