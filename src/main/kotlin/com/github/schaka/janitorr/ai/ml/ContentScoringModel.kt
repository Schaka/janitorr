package com.github.schaka.janitorr.ai.ml

import com.github.schaka.janitorr.ai.data.MediaFeatures

/**
 * Content scoring model interface for ML predictions.
 * 
 * This is a placeholder interface for future AI/ML implementation.
 * Implementations will use trained ML models to score media content.
 */
interface ContentScoringModel {
    
    /**
     * Predict probability that media should be kept (0.0 to 1.0)
     * 
     * @param features Feature vector for the media item
     * @return Probability score (0.0 = definitely delete, 1.0 = definitely keep)
     */
    fun predictKeepProbability(features: MediaFeatures): Double
    
    /**
     * Generate human-readable explanation of the prediction
     * 
     * @param features Feature vector used for prediction
     * @param score Prediction score
     * @return Human-readable explanation string
     */
    fun explainPrediction(features: MediaFeatures, score: Double): String
    
    /**
     * Get feature importance for this specific prediction
     * 
     * @param features Feature vector used for prediction
     * @return Map of feature names to importance scores (higher = more influential)
     */
    fun getFeatureImportance(features: MediaFeatures): Map<String, Double>
    
    /**
     * Get model metadata
     * 
     * @return Information about the model (version, accuracy, training date, etc.)
     */
    fun getModelMetadata(): ModelMetadata
}

/**
 * Metadata about a trained ML model
 */
data class ModelMetadata(
    val version: String,
    val algorithm: String,
    val trainedAt: String,
    val accuracy: Double,
    val precision: Double,
    val recall: Double,
    val f1Score: Double
)

/**
 * Training result containing the trained model and metrics
 */
data class TrainingResult(
    val model: ContentScoringModel,
    val metrics: ModelMetrics
)

/**
 * Metrics from model training/validation
 */
data class ModelMetrics(
    val accuracy: Double,
    val precision: Double,
    val recall: Double,
    val f1Score: Double,
    val confusionMatrix: ConfusionMatrix
)

/**
 * Confusion matrix for binary classification
 */
data class ConfusionMatrix(
    val truePositives: Int,
    val trueNegatives: Int,
    val falsePositives: Int,
    val falseNegatives: Int
) {
    val total: Int
        get() = truePositives + trueNegatives + falsePositives + falseNegatives
}
