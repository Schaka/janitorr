package com.github.schaka.janitorr.config.service

import com.github.schaka.janitorr.api.dto.*
import com.github.schaka.janitorr.config.*
import com.github.schaka.janitorr.jellyseerr.JellyseerrProperties
import com.github.schaka.janitorr.mediaserver.emby.EmbyProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.servarr.bazarr.BazarrProperties
import com.github.schaka.janitorr.servarr.radarr.RadarrProperties
import com.github.schaka.janitorr.servarr.sonarr.SonarrProperties
import com.github.schaka.janitorr.stats.jellystat.JellystatProperties
import com.github.schaka.janitorr.stats.streamystats.StreamystatsProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service for managing application configuration through the Web UI.
 * 
 * Handles:
 * - Reading current configuration from properties
 * - Writing configuration updates to application.yml
 * - Creating backups before modifications
 * - Importing/exporting configuration
 * - Resetting to default values
 */
@Profile("!leyden")
@ConditionalOnProperty(prefix = "management.ui", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Service
class ConfigurationService(
    private val applicationProperties: ApplicationProperties,
    private val fileSystemProperties: FileSystemProperties,
    private val sonarrProperties: SonarrProperties,
    private val radarrProperties: RadarrProperties,
    private val bazarrProperties: BazarrProperties,
    private val jellyfinProperties: JellyfinProperties,
    private val embyProperties: EmbyProperties,
    private val jellyseerrProperties: JellyseerrProperties,
    private val jellystatProperties: JellystatProperties,
    private val streamystatsProperties: StreamystatsProperties,
    private val managementUiProperties: ManagementUiProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(ConfigurationService::class.java)
        private const val CONFIG_FILE_PATH = "/config/application.yml"
        private const val BACKUP_DIR = "/config/backups"
    }

    /**
     * Get current configuration as DTO
     */
    fun getCurrentConfiguration(): ConfigurationDto {
        return ConfigurationDto(
            application = ApplicationConfigDto(
                dryRun = applicationProperties.dryRun,
                runOnce = applicationProperties.runOnce,
                wholeTvShow = applicationProperties.wholeTvShow,
                wholeShowSeedingCheck = applicationProperties.wholeShowSeedingCheck,
                leavingSoon = durationToString(applicationProperties.leavingSoon),
                exclusionTags = applicationProperties.exclusionTags,
                mediaDeletion = MediaDeletionConfigDto(
                    enabled = applicationProperties.mediaDeletion.enabled,
                    movieExpiration = applicationProperties.mediaDeletion.movieExpiration.mapValues { durationToString(it.value) },
                    seasonExpiration = applicationProperties.mediaDeletion.seasonExpiration.mapValues { durationToString(it.value) }
                ),
                tagBasedDeletion = TagDeletionConfigDto(
                    enabled = applicationProperties.tagBasedDeletion.enabled,
                    minimumFreeDiskPercent = applicationProperties.tagBasedDeletion.minimumFreeDiskPercent,
                    schedules = applicationProperties.tagBasedDeletion.schedules.map {
                        TagScheduleDto(it.tag, durationToString(it.expiration))
                    }
                ),
                episodeDeletion = EpisodeDeletionConfigDto(
                    enabled = applicationProperties.episodeDeletion.enabled,
                    tag = applicationProperties.episodeDeletion.tag,
                    maxEpisodes = applicationProperties.episodeDeletion.maxEpisodes,
                    maxAge = durationToString(applicationProperties.episodeDeletion.maxAge)
                )
            ),
            fileSystem = FileSystemConfigDto(
                access = fileSystemProperties.access,
                validateSeeding = fileSystemProperties.validateSeeding,
                leavingSoonDir = fileSystemProperties.leavingSoonDir,
                mediaServerLeavingSoonDir = fileSystemProperties.mediaServerLeavingSoonDir,
                fromScratch = fileSystemProperties.fromScratch,
                freeSpaceCheckDir = fileSystemProperties.freeSpaceCheckDir
            ),
            clients = ClientsConfigDto(
                sonarr = SonarrConfigDto(
                    enabled = sonarrProperties.enabled,
                    url = sonarrProperties.url,
                    apiKey = sonarrProperties.apiKey,
                    deleteEmptyShows = sonarrProperties.deleteEmptyShows,
                    determineAgeBy = sonarrProperties.determineAgeBy?.name,
                    importExclusions = sonarrProperties.importExclusions
                ),
                radarr = RadarrConfigDto(
                    enabled = radarrProperties.enabled,
                    url = radarrProperties.url,
                    apiKey = radarrProperties.apiKey,
                    onlyDeleteFiles = radarrProperties.onlyDeleteFiles,
                    determineAgeBy = radarrProperties.determineAgeBy?.name,
                    importExclusions = radarrProperties.importExclusions
                ),
                bazarr = BazarrConfigDto(
                    enabled = bazarrProperties.enabled,
                    url = bazarrProperties.url,
                    apiKey = bazarrProperties.apiKey
                ),
                jellyfin = JellyfinConfigDto(
                    enabled = jellyfinProperties.enabled,
                    url = jellyfinProperties.url,
                    apiKey = jellyfinProperties.apiKey,
                    username = jellyfinProperties.username,
                    password = jellyfinProperties.password,
                    delete = jellyfinProperties.delete,
                    leavingSoonTv = jellyfinProperties.leavingSoonTv,
                    leavingSoonMovies = jellyfinProperties.leavingSoonMovies,
                    leavingSoonType = jellyfinProperties.leavingSoonType.name
                ),
                emby = EmbyConfigDto(
                    enabled = embyProperties.enabled,
                    url = embyProperties.url,
                    apiKey = embyProperties.apiKey,
                    username = embyProperties.username,
                    password = embyProperties.password,
                    delete = embyProperties.delete,
                    leavingSoonTv = embyProperties.leavingSoonTv,
                    leavingSoonMovies = embyProperties.leavingSoonMovies,
                    leavingSoonType = embyProperties.leavingSoonType.name
                ),
                jellyseerr = JellyseerrConfigDto(
                    enabled = jellyseerrProperties.enabled,
                    url = jellyseerrProperties.url,
                    apiKey = jellyseerrProperties.apiKey,
                    matchServer = jellyseerrProperties.matchServer
                ),
                jellystat = JellystatConfigDto(
                    enabled = jellystatProperties.enabled,
                    wholeTvShow = jellystatProperties.wholeTvShow,
                    url = jellystatProperties.url,
                    apiKey = jellystatProperties.apiKey
                ),
                streamystats = StreamystatsConfigDto(
                    enabled = streamystatsProperties.enabled,
                    wholeTvShow = streamystatsProperties.wholeTvShow,
                    url = streamystatsProperties.url,
                    apiKey = streamystatsProperties.apiKey
                )
            ),
            management = ManagementConfigDto(
                ui = ManagementUiConfigDto(
                    enabled = managementUiProperties.enabled
                )
            )
        )
    }

    /**
     * Update configuration and save to file
     */
    fun updateConfiguration(config: ConfigurationDto) {
        // Create backup before making changes
        createBackup()
        
        // Convert DTO to YAML and write to file
        val yamlMap = configDtoToMap(config)
        writeConfigurationToFile(yamlMap)
        
        log.info("Configuration updated successfully")
    }

    /**
     * Export configuration as YAML string
     */
    fun exportConfiguration(): String {
        val configFile = File(CONFIG_FILE_PATH)
        if (!configFile.exists()) {
            throw IllegalStateException("Configuration file not found: $CONFIG_FILE_PATH")
        }
        return configFile.readText()
    }

    /**
     * Import configuration from YAML string
     */
    fun importConfiguration(yamlContent: String) {
        // Validate YAML by parsing it
        try {
            val yaml = Yaml()
            yaml.load<Map<String, Any>>(yamlContent)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid YAML format: ${e.message}")
        }
        
        // Create backup before importing
        createBackup()
        
        // Write new configuration
        val configFile = File(CONFIG_FILE_PATH)
        configFile.writeText(yamlContent)
        
        log.info("Configuration imported successfully")
    }

    /**
     * Create backup of current configuration
     */
    fun createBackup(): String {
        val backupDir = File(BACKUP_DIR)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val backupFile = File(backupDir, "application_$timestamp.yml")
        
        val configFile = File(CONFIG_FILE_PATH)
        if (configFile.exists()) {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            log.info("Configuration backup created: ${backupFile.name}")
            return backupFile.name
        } else {
            throw IllegalStateException("Configuration file not found: $CONFIG_FILE_PATH")
        }
    }

    /**
     * Restore configuration from backup
     */
    fun restoreFromBackup(backupFileName: String) {
        val backupFile = File(BACKUP_DIR, backupFileName)
        if (!backupFile.exists()) {
            throw IllegalArgumentException("Backup file not found: $backupFileName")
        }
        
        // Create a backup of current state before restoring
        createBackup()
        
        val configFile = File(CONFIG_FILE_PATH)
        Files.copy(backupFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        
        log.info("Configuration restored from backup: $backupFileName")
    }

    /**
     * List available backups
     */
    fun listBackups(): List<String> {
        val backupDir = File(BACKUP_DIR)
        if (!backupDir.exists()) {
            return emptyList()
        }
        
        return backupDir.listFiles { file ->
            file.isFile && file.name.startsWith("application_") && file.name.endsWith(".yml")
        }?.map { it.name }?.sortedDescending() ?: emptyList()
    }

    /**
     * Reset configuration to defaults (from template)
     */
    fun resetToDefaults() {
        // Create backup before resetting
        createBackup()
        
        // Copy template to config location
        val templateResource = this::class.java.classLoader.getResourceAsStream("application-template.yml")
            ?: throw IllegalStateException("Template file not found")
        
        val configFile = File(CONFIG_FILE_PATH)
        configFile.outputStream().use { output ->
            templateResource.copyTo(output)
        }
        
        log.info("Configuration reset to defaults")
    }

    /**
     * Convert Duration to string format (e.g., "14d", "30d")
     */
    private fun durationToString(duration: Duration): String {
        val days = duration.toDays()
        return "${days}d"
    }

    /**
     * Convert ConfigurationDto to nested Map for YAML serialization
     */
    private fun configDtoToMap(config: ConfigurationDto): Map<String, Any> {
        return mapOf(
            "application" to mapOf(
                "dry-run" to config.application.dryRun,
                "run-once" to config.application.runOnce,
                "whole-tv-show" to config.application.wholeTvShow,
                "whole-show-seeding-check" to config.application.wholeShowSeedingCheck,
                "leaving-soon" to config.application.leavingSoon,
                "exclusion-tags" to config.application.exclusionTags,
                "media-deletion" to mapOf(
                    "enabled" to config.application.mediaDeletion.enabled,
                    "movie-expiration" to config.application.mediaDeletion.movieExpiration,
                    "season-expiration" to config.application.mediaDeletion.seasonExpiration
                ),
                "tag-based-deletion" to mapOf(
                    "enabled" to config.application.tagBasedDeletion.enabled,
                    "minimum-free-disk-percent" to config.application.tagBasedDeletion.minimumFreeDiskPercent,
                    "schedules" to config.application.tagBasedDeletion.schedules.map {
                        mapOf("tag" to it.tag, "expiration" to it.expiration)
                    }
                ),
                "episode-deletion" to mapOf(
                    "enabled" to config.application.episodeDeletion.enabled,
                    "tag" to config.application.episodeDeletion.tag,
                    "max-episodes" to config.application.episodeDeletion.maxEpisodes,
                    "max-age" to config.application.episodeDeletion.maxAge
                )
            ),
            "file-system" to mapOf(
                "access" to config.fileSystem.access,
                "validate-seeding" to config.fileSystem.validateSeeding,
                "leaving-soon-dir" to config.fileSystem.leavingSoonDir,
                "media-server-leaving-soon-dir" to config.fileSystem.mediaServerLeavingSoonDir,
                "from-scratch" to config.fileSystem.fromScratch,
                "free-space-check-dir" to config.fileSystem.freeSpaceCheckDir
            ),
            "clients" to mapOf(
                "sonarr" to mapOf(
                    "enabled" to config.clients.sonarr.enabled,
                    "url" to config.clients.sonarr.url,
                    "api-key" to config.clients.sonarr.apiKey,
                    "delete-empty-shows" to config.clients.sonarr.deleteEmptyShows,
                    "determine-age-by" to config.clients.sonarr.determineAgeBy,
                    "import-exclusions" to config.clients.sonarr.importExclusions
                ),
                "radarr" to mapOf(
                    "enabled" to config.clients.radarr.enabled,
                    "url" to config.clients.radarr.url,
                    "api-key" to config.clients.radarr.apiKey,
                    "only-delete-files" to config.clients.radarr.onlyDeleteFiles,
                    "determine-age-by" to config.clients.radarr.determineAgeBy,
                    "import-exclusions" to config.clients.radarr.importExclusions
                ),
                "bazarr" to mapOf(
                    "enabled" to config.clients.bazarr.enabled,
                    "url" to config.clients.bazarr.url,
                    "api-key" to config.clients.bazarr.apiKey
                ),
                "jellyfin" to mapOf(
                    "enabled" to config.clients.jellyfin.enabled,
                    "url" to config.clients.jellyfin.url,
                    "api-key" to config.clients.jellyfin.apiKey,
                    "username" to config.clients.jellyfin.username,
                    "password" to config.clients.jellyfin.password,
                    "delete" to config.clients.jellyfin.delete,
                    "leaving-soon-tv" to config.clients.jellyfin.leavingSoonTv,
                    "leaving-soon-movies" to config.clients.jellyfin.leavingSoonMovies,
                    "leaving-soon-type" to config.clients.jellyfin.leavingSoonType
                ),
                "emby" to mapOf(
                    "enabled" to config.clients.emby.enabled,
                    "url" to config.clients.emby.url,
                    "api-key" to config.clients.emby.apiKey,
                    "username" to config.clients.emby.username,
                    "password" to config.clients.emby.password,
                    "delete" to config.clients.emby.delete,
                    "leaving-soon-tv" to config.clients.emby.leavingSoonTv,
                    "leaving-soon-movies" to config.clients.emby.leavingSoonMovies,
                    "leaving-soon-type" to config.clients.emby.leavingSoonType
                ),
                "jellyseerr" to mapOf(
                    "enabled" to config.clients.jellyseerr.enabled,
                    "url" to config.clients.jellyseerr.url,
                    "api-key" to config.clients.jellyseerr.apiKey,
                    "match-server" to config.clients.jellyseerr.matchServer
                ),
                "jellystat" to mapOf(
                    "enabled" to config.clients.jellystat.enabled,
                    "whole-tv-show" to config.clients.jellystat.wholeTvShow,
                    "url" to config.clients.jellystat.url,
                    "api-key" to config.clients.jellystat.apiKey
                ),
                "streamystats" to mapOf(
                    "enabled" to config.clients.streamystats.enabled,
                    "whole-tv-show" to config.clients.streamystats.wholeTvShow,
                    "url" to config.clients.streamystats.url,
                    "api-key" to config.clients.streamystats.apiKey
                )
            ),
            "management" to mapOf(
                "ui" to mapOf(
                    "enabled" to config.management.ui.enabled
                )
            )
        )
    }

    /**
     * Write configuration map to YAML file
     */
    private fun writeConfigurationToFile(configMap: Map<String, Any>) {
        val dumperOptions = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        }
        val yaml = Yaml(dumperOptions)
        
        val configFile = File(CONFIG_FILE_PATH)
        configFile.writeText(yaml.dump(configMap))
    }
}
