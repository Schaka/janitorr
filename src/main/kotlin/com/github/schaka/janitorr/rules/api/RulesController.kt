package com.github.schaka.janitorr.rules.api

import com.github.schaka.janitorr.rules.engine.RuleEngineService
import com.github.schaka.janitorr.rules.engine.ValidationResult
import com.github.schaka.janitorr.rules.model.CustomRule
import com.github.schaka.janitorr.rules.storage.RuleStorageService
import com.github.schaka.janitorr.servarr.ServarrService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

/**
 * REST API controller for managing custom cleanup rules
 */
@Profile("!leyden")
@ConditionalOnProperty(prefix = "rule-engine", name = ["enabled"], havingValue = "true")
@RestController
@RequestMapping("/api/rules")
class RulesController(
    private val ruleEngineService: RuleEngineService,
    private val ruleStorageService: RuleStorageService,
    private val sonarrService: ServarrService,
    private val radarrService: ServarrService
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @GetMapping
    fun getAllRules(): List<CustomRule> {
        log.info("Fetching all rules")
        return ruleStorageService.getAllRules()
    }

    @GetMapping("/{id}")
    fun getRule(@PathVariable id: String): CustomRule? {
        log.info("Fetching rule with id: {}", id)
        return ruleStorageService.getRule(id)
    }

    @PostMapping
    fun createRule(@RequestBody rule: CustomRule): Map<String, Any> {
        log.info("Creating new rule: {}", rule.name)
        
        val validation = ruleEngineService.validateRule(rule)
        if (!validation.isValid) {
            return mapOf(
                "success" to false,
                "message" to "Rule validation failed: ${validation.errorMessage}"
            )
        }

        ruleStorageService.saveRule(rule)
        return mapOf(
            "success" to true,
            "message" to "Rule created successfully",
            "ruleId" to rule.id
        )
    }

    @PutMapping("/{id}")
    fun updateRule(@PathVariable id: String, @RequestBody rule: CustomRule): Map<String, Any> {
        log.info("Updating rule with id: {}", id)
        
        val validation = ruleEngineService.validateRule(rule)
        if (!validation.isValid) {
            return mapOf(
                "success" to false,
                "message" to "Rule validation failed: ${validation.errorMessage}"
            )
        }

        ruleStorageService.saveRule(rule.copy(id = id))
        return mapOf(
            "success" to true,
            "message" to "Rule updated successfully"
        )
    }

    @DeleteMapping("/{id}")
    fun deleteRule(@PathVariable id: String): Map<String, Any> {
        log.info("Deleting rule with id: {}", id)
        ruleStorageService.deleteRule(id)
        return mapOf(
            "success" to true,
            "message" to "Rule deleted successfully"
        )
    }

    @PostMapping("/{id}/validate")
    fun validateRule(@PathVariable id: String): Map<String, Any> {
        log.info("Validating rule with id: {}", id)
        val rule = ruleStorageService.getRule(id)
        
        if (rule == null) {
            return mapOf(
                "success" to false,
                "message" to "Rule not found"
            )
        }

        val validation = ruleEngineService.validateRule(rule)
        return mapOf(
            "success" to validation.isValid,
            "message" to (validation.errorMessage ?: "Rule is valid"),
            "isValid" to validation.isValid
        )
    }

    @PostMapping("/{id}/preview")
    fun previewRule(@PathVariable id: String, @RequestParam(required = false, defaultValue = "movies") type: String): Map<String, Any> {
        log.info("Previewing rule with id: {}", id)
        val rule = ruleStorageService.getRule(id)
        
        if (rule == null) {
            return mapOf(
                "success" to false,
                "message" to "Rule not found"
            )
        }

        val service = if (type == "tv") sonarrService else radarrService
        val allMedia = service.getEntries()
        val matchingMedia = ruleEngineService.previewRule(rule, allMedia)

        return mapOf(
            "success" to true,
            "totalMediaItems" to allMedia.size,
            "matchingItems" to matchingMedia.size,
            "matchedMedia" to matchingMedia.map { mapOf(
                "id" to it.id,
                "path" to it.libraryPath,
                "importedDate" to it.importedDate,
                "imdbId" to it.imdbId
            ) }
        )
    }

    @PostMapping("/{id}/execute")
    fun executeRule(@PathVariable id: String, @RequestParam(required = false, defaultValue = "true") dryRun: Boolean): Map<String, Any> {
        log.info("Executing rule with id: {} (dryRun: {})", id, dryRun)
        val rule = ruleStorageService.getRule(id)
        
        if (rule == null) {
            return mapOf(
                "success" to false,
                "message" to "Rule not found"
            )
        }

        if (!rule.enabled) {
            return mapOf(
                "success" to false,
                "message" to "Rule is disabled"
            )
        }

        try {
            val allMedia = sonarrService.getEntries() + radarrService.getEntries()
            val matchingMedia = ruleEngineService.previewRule(rule, allMedia)
            
            matchingMedia.forEach { mediaItem ->
                ruleEngineService.executeActions(rule, mediaItem, dryRun)
            }

            return mapOf(
                "success" to true,
                "message" to if (dryRun) "Rule executed in dry-run mode" else "Rule executed successfully",
                "processedItems" to matchingMedia.size,
                "dryRun" to dryRun
            )
        } catch (e: Exception) {
            log.error("Error executing rule", e)
            return mapOf(
                "success" to false,
                "message" to "Error executing rule: ${e.message}"
            )
        }
    }
}
