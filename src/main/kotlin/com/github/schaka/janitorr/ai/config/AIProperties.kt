package com.github.schaka.janitorr.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for AI/ML Engine.
 * 
 * This is a placeholder for future AI/ML features.
 * All AI features are disabled by default and require explicit opt-in.
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
data class AIProperties(
    /**
     * Enable/disable all AI features
     * Default: false (AI features disabled)
     */
    var enabled: Boolean = false,
    
    /**
     * Path where ML models are stored
     * Default: /config/models
     */
    var modelPath: String = "/config/models",
    
    /**
     * Training configuration
     */
    var training: TrainingProperties = TrainingProperties(),
    
    /**
     * Inference configuration
     */
    var inference: InferenceProperties = InferenceProperties(),
    
    /**
     * Feature flags for different AI capabilities
     */
    var features: FeatureProperties = FeatureProperties()
)

/**
 * Configuration for ML model training
 */
data class TrainingProperties(
    /**
     * Enable automatic model training
     * Default: false (manual training only)
     */
    var enabled: Boolean = false,
    
    /**
     * Cron schedule for automatic training (if enabled)
     * Default: "0 0 3 * * ?" (3 AM daily)
     */
    var schedule: String = "0 0 3 * * ?",
    
    /**
     * Minimum number of data points required before training
     * Default: 1000
     */
    var minDataPoints: Int = 1000,
    
    /**
     * Number of days of historical data to use for training
     * Default: 90 days
     */
    var historicalDataDays: Int = 90
)

/**
 * Configuration for ML inference engine
 */
data class InferenceProperties(
    /**
     * Time-to-live for cached predictions (seconds)
     * Default: 3600 (1 hour)
     */
    var cacheTtl: Int = 3600,
    
    /**
     * Number of items to process in each batch
     * Default: 100
     */
    var batchSize: Int = 100,
    
    /**
     * Minimum confidence threshold to act on predictions (0.0 to 1.0)
     * Default: 0.7 (70% confidence)
     */
    var confidenceThreshold: Double = 0.7,
    
    /**
     * Maximum time to wait for prediction (milliseconds)
     * Default: 100ms
     */
    var timeoutMs: Long = 100
)

/**
 * Feature flags for different AI capabilities
 */
data class FeatureProperties(
    /**
     * Enable external API integrations for enrichment
     * Default: false (privacy-preserving, local-only processing)
     */
    var externalApis: Boolean = false,
    
    /**
     * Enable learning from user feedback and corrections
     * Default: true (recommended for model improvement)
     */
    var userFeedback: Boolean = true,
    
    /**
     * Enable natural language query interface
     * Default: false (future feature)
     */
    var naturalLanguage: Boolean = false,
    
    /**
     * Enable computer vision features
     * Default: false (future feature)
     */
    var computerVision: Boolean = false
)
