package com.github.schaka.janitorr.rules.engine

import com.github.schaka.janitorr.rules.model.*
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Core rule engine service that evaluates and executes rules.
 */
@Service
class RuleEngineService(
    private val conditionEvaluator: ConditionEvaluator,
    private val actionExecutor: ActionExecutor
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    /**
     * Evaluates a rule against a media item
     * @return true if the rule conditions are met
     */
    fun evaluateRule(rule: CustomRule, mediaItem: LibraryItem): Boolean {
        if (!rule.enabled) {
            log.debug("Rule {} is disabled, skipping evaluation", rule.name)
            return false
        }

        if (rule.conditions.isEmpty()) {
            log.warn("Rule {} has no conditions, returning false", rule.name)
            return false
        }

        return when (rule.logicOperator) {
            LogicOperator.AND -> rule.conditions.all { conditionEvaluator.evaluate(it, mediaItem) }
            LogicOperator.OR -> rule.conditions.any { conditionEvaluator.evaluate(it, mediaItem) }
        }
    }

    /**
     * Executes actions for a rule on a media item
     */
    fun executeActions(rule: CustomRule, mediaItem: LibraryItem, dryRun: Boolean = false) {
        if (rule.actions.isEmpty()) {
            log.warn("Rule {} has no actions to execute", rule.name)
            return
        }

        log.info("Executing {} actions for rule {} on media {}", 
            rule.actions.size, rule.name, mediaItem.libraryPath)

        rule.actions.forEach { action ->
            try {
                if (dryRun) {
                    log.info("[DRY RUN] Would execute action {} on {}", action.type, mediaItem.libraryPath)
                } else {
                    actionExecutor.execute(action, mediaItem)
                }
            } catch (e: Exception) {
                log.error("Failed to execute action {} for rule {} on media {}", 
                    action.type, rule.name, mediaItem.libraryPath, e)
            }
        }
    }

    /**
     * Preview which media items would be affected by a rule
     */
    fun previewRule(rule: CustomRule, mediaItems: List<LibraryItem>): List<LibraryItem> {
        return mediaItems.filter { evaluateRule(rule, it) }
    }

    /**
     * Validates a rule for correctness
     */
    fun validateRule(rule: CustomRule): ValidationResult {
        val errors = mutableListOf<String>()

        if (rule.name.isBlank()) {
            errors.add("Rule name cannot be blank")
        }

        if (rule.conditions.isEmpty()) {
            errors.add("Rule must have at least one condition")
        }

        if (rule.actions.isEmpty()) {
            errors.add("Rule must have at least one action")
        }

        // Validate conditions
        rule.conditions.forEach { condition ->
            when (condition) {
                is AgeCondition -> {
                    if (condition.days < 0) {
                        errors.add("Age condition days must be non-negative")
                    }
                }
                is SizeCondition -> {
                    if (condition.sizeInGB <= 0) {
                        errors.add("Size condition must be positive")
                    }
                }
                is DiskUsageCondition -> {
                    if (condition.percentage < 0 || condition.percentage > 100) {
                        errors.add("Disk usage percentage must be between 0 and 100")
                    }
                }
                is RatingCondition -> {
                    if (condition.rating < 0 || condition.rating > 10) {
                        errors.add("Rating must be between 0 and 10")
                    }
                }
                else -> {} // Other conditions validated during evaluation
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult(true, null)
        } else {
            ValidationResult(false, errors.joinToString(", "))
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)
