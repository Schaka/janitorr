package com.github.schaka.janitorr.rules.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the rule engine
 */
@ConfigurationProperties(prefix = "rule-engine")
data class RuleEngineProperties(
    val enabled: Boolean = false,
    val rulesDirectory: String = "/config/rules",
    val maxRulesPerExecution: Int = 100,
    val enableScheduledRules: Boolean = false
)
