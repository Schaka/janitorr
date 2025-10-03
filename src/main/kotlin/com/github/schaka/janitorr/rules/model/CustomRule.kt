package com.github.schaka.janitorr.rules.model

import java.time.LocalDateTime

/**
 * Represents a custom cleanup rule with conditions and actions.
 * Rules are evaluated against media items and execute actions when conditions are met.
 */
data class CustomRule(
    val id: String,
    val name: String,
    val description: String? = null,
    val enabled: Boolean = true,
    val conditions: List<Condition>,
    val actions: List<Action>,
    val logicOperator: LogicOperator = LogicOperator.AND,
    val schedule: RuleSchedule? = null,
    val priority: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Defines how multiple conditions are combined
 */
enum class LogicOperator {
    AND,
    OR
}

/**
 * Optional scheduling configuration for automatic rule execution
 */
data class RuleSchedule(
    val cronExpression: String,
    val enabled: Boolean = true
)
