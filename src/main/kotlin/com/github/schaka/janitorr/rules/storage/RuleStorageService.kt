package com.github.schaka.janitorr.rules.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.schaka.janitorr.rules.config.RuleEngineProperties
import com.github.schaka.janitorr.rules.model.CustomRule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Service for storing and retrieving rules from the filesystem
 */
@Service
class RuleStorageService(
    private val ruleEngineProperties: RuleEngineProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private const val RULES_FILE_EXTENSION = ".json"
    }

    private val objectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    private val rulesDir: File by lazy {
        val dir = File(ruleEngineProperties.rulesDirectory)
        if (!dir.exists()) {
            dir.mkdirs()
            log.info("Created rules directory: {}", dir.absolutePath)
        }
        dir
    }

    fun saveRule(rule: CustomRule) {
        try {
            val file = File(rulesDir, "${rule.id}$RULES_FILE_EXTENSION")
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, rule)
            log.info("Saved rule {} to {}", rule.name, file.absolutePath)
        } catch (e: Exception) {
            log.error("Failed to save rule {}", rule.name, e)
            throw e
        }
    }

    fun getRule(id: String): CustomRule? {
        return try {
            val file = File(rulesDir, "$id$RULES_FILE_EXTENSION")
            if (!file.exists()) {
                log.warn("Rule file not found: {}", file.absolutePath)
                return null
            }
            objectMapper.readValue<CustomRule>(file)
        } catch (e: Exception) {
            log.error("Failed to load rule with id {}", id, e)
            null
        }
    }

    fun getAllRules(): List<CustomRule> {
        return try {
            rulesDir.listFiles { file -> file.name.endsWith(RULES_FILE_EXTENSION) }
                ?.mapNotNull { file ->
                    try {
                        objectMapper.readValue<CustomRule>(file)
                    } catch (e: Exception) {
                        log.error("Failed to load rule from {}", file.absolutePath, e)
                        null
                    }
                } ?: emptyList()
        } catch (e: Exception) {
            log.error("Failed to load rules", e)
            emptyList()
        }
    }

    fun deleteRule(id: String) {
        try {
            val file = File(rulesDir, "$id$RULES_FILE_EXTENSION")
            if (file.exists()) {
                file.delete()
                log.info("Deleted rule with id {}", id)
            } else {
                log.warn("Rule file not found for deletion: {}", id)
            }
        } catch (e: Exception) {
            log.error("Failed to delete rule with id {}", id, e)
            throw e
        }
    }

    fun getRuleCount(): Int {
        return rulesDir.listFiles { file -> file.name.endsWith(RULES_FILE_EXTENSION) }?.size ?: 0
    }
}
