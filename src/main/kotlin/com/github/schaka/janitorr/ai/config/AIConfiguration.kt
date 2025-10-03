package com.github.schaka.janitorr.ai.config

import com.github.schaka.janitorr.ai.inference.DisabledInferenceEngine
import com.github.schaka.janitorr.ai.inference.InferenceEngine
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * AI/ML Engine configuration.
 * 
 * Provides beans for AI features when enabled.
 * By default, all AI features are disabled and a no-op implementation is provided.
 */
@Configuration
class AIConfiguration(
    private val aiProperties: AIProperties
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }
    
    /**
     * Provides a disabled inference engine when AI features are not enabled.
     * This is the default state.
     */
    @Bean
    @ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "false", matchIfMissing = true)
    fun disabledInferenceEngine(): InferenceEngine {
        log.info("AI features are disabled. To enable, set ai.enabled=true in configuration.")
        return DisabledInferenceEngine()
    }
    
    // Future: When AI features are implemented, add beans here with
    // @ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "true")
    // 
    // Example:
    // @Bean
    // @ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "true")
    // fun inferenceEngine(): InferenceEngine {
    //     return MLInferenceEngine(aiProperties, contentScoringModel())
    // }
}
