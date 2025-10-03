package com.github.schaka.janitorr.api

import com.github.schaka.janitorr.api.dto.ConfigurationDto
import com.github.schaka.janitorr.api.dto.ConnectionTestResult
import com.github.schaka.janitorr.api.dto.ConnectionTestsResult
import com.github.schaka.janitorr.config.service.ConfigurationService
import com.github.schaka.janitorr.config.service.ConnectionTestService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for configuration management via Web UI.
 * 
 * Provides endpoints for:
 * - Retrieving current configuration
 * - Updating configuration
 * - Testing connections to external services
 * - Importing/exporting configuration
 * - Backup and restore operations
 * 
 * IMPORTANT: This controller is excluded from the "leyden" profile (@Profile("!leyden")).
 * See ManagementController documentation for details about profile usage.
 */
@Profile("!leyden")
@ConditionalOnProperty(prefix = "management.ui", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@RestController
@RequestMapping("/api/management/config")
class ConfigurationController(
    private val configurationService: ConfigurationService,
    private val connectionTestService: ConnectionTestService
) {

    companion object {
        private val log = LoggerFactory.getLogger(ConfigurationController::class.java)
    }

    @GetMapping
    fun getConfiguration(): ResponseEntity<ConfigurationDto> {
        log.info("Fetching current configuration")
        return try {
            val config = configurationService.getCurrentConfiguration()
            ResponseEntity.ok(config)
        } catch (e: Exception) {
            log.error("Error fetching configuration", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PutMapping
    fun updateConfiguration(@RequestBody config: ConfigurationDto): ResponseEntity<Map<String, Any>> {
        log.info("Updating configuration via Web UI")
        return try {
            configurationService.updateConfiguration(config)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Configuration updated successfully. Restart required for changes to take effect.",
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            log.error("Error updating configuration", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Error updating configuration: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }

    @PostMapping("/test")
    fun testConnections(@RequestBody(required = false) config: ConfigurationDto?): ResponseEntity<ConnectionTestsResult> {
        log.info("Testing connections to external services")
        return try {
            val configToTest = config ?: configurationService.getCurrentConfiguration()
            val results = connectionTestService.testAllConnections(configToTest)
            ResponseEntity.ok(results)
        } catch (e: Exception) {
            log.error("Error testing connections", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/test/sonarr")
    fun testSonarrConnection(@RequestBody config: ConfigurationDto): ResponseEntity<ConnectionTestResult> {
        log.info("Testing Sonarr connection")
        return ResponseEntity.ok(connectionTestService.testSonarr(config.clients.sonarr))
    }

    @PostMapping("/test/radarr")
    fun testRadarrConnection(@RequestBody config: ConfigurationDto): ResponseEntity<ConnectionTestResult> {
        log.info("Testing Radarr connection")
        return ResponseEntity.ok(connectionTestService.testRadarr(config.clients.radarr))
    }

    @PostMapping("/test/jellyfin")
    fun testJellyfinConnection(@RequestBody config: ConfigurationDto): ResponseEntity<ConnectionTestResult> {
        log.info("Testing Jellyfin connection")
        return ResponseEntity.ok(connectionTestService.testJellyfin(config.clients.jellyfin))
    }

    @PostMapping("/test/emby")
    fun testEmbyConnection(@RequestBody config: ConfigurationDto): ResponseEntity<ConnectionTestResult> {
        log.info("Testing Emby connection")
        return ResponseEntity.ok(connectionTestService.testEmby(config.clients.emby))
    }

    @PostMapping("/test/jellyseerr")
    fun testJellyseerrConnection(@RequestBody config: ConfigurationDto): ResponseEntity<ConnectionTestResult> {
        log.info("Testing Jellyseerr connection")
        return ResponseEntity.ok(connectionTestService.testJellyseerr(config.clients.jellyseerr))
    }

    @GetMapping("/export")
    fun exportConfiguration(): ResponseEntity<String> {
        log.info("Exporting configuration")
        return try {
            val yamlContent = configurationService.exportConfiguration()
            ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=application.yml")
                .header("Content-Type", "application/x-yaml")
                .body(yamlContent)
        } catch (e: Exception) {
            log.error("Error exporting configuration", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/import")
    fun importConfiguration(@RequestBody yamlContent: String): ResponseEntity<Map<String, Any>> {
        log.info("Importing configuration")
        return try {
            configurationService.importConfiguration(yamlContent)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Configuration imported successfully. Restart required for changes to take effect.",
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            log.error("Error importing configuration", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
                "success" to false,
                "message" to "Error importing configuration: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }

    @PostMapping("/backup")
    fun backupConfiguration(): ResponseEntity<Map<String, Any>> {
        log.info("Creating configuration backup")
        return try {
            val backupFile = configurationService.createBackup()
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Configuration backup created successfully",
                "backupFile" to backupFile,
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            log.error("Error creating configuration backup", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Error creating backup: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }

    @PostMapping("/restore")
    fun restoreConfiguration(@RequestParam backupFile: String): ResponseEntity<Map<String, Any>> {
        log.info("Restoring configuration from backup: $backupFile")
        return try {
            configurationService.restoreFromBackup(backupFile)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Configuration restored successfully. Restart required for changes to take effect.",
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            log.error("Error restoring configuration", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
                "success" to false,
                "message" to "Error restoring configuration: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }

    @GetMapping("/backups")
    fun listBackups(): ResponseEntity<Map<String, Any>> {
        log.info("Listing available backups")
        return try {
            val backups = configurationService.listBackups()
            ResponseEntity.ok(mapOf(
                "backups" to backups,
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            log.error("Error listing backups", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "backups" to emptyList<String>(),
                "error" to e.message,
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }

    @PostMapping("/reset")
    fun resetToDefaults(): ResponseEntity<Map<String, Any>> {
        log.info("Resetting configuration to defaults")
        return try {
            configurationService.resetToDefaults()
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Configuration reset to defaults. Restart required for changes to take effect.",
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            log.error("Error resetting configuration", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "Error resetting configuration: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }
}
