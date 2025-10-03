package com.github.schaka.janitorr.api.dto

import com.github.schaka.janitorr.mediaserver.LeavingSoonType
import com.github.schaka.janitorr.servarr.HistorySort
import java.time.Duration

/**
 * Complete configuration DTO for web UI management
 */
data class ConfigurationDto(
    val application: ApplicationConfigDto,
    val fileSystem: FileSystemConfigDto,
    val clients: ClientsConfigDto,
    val management: ManagementConfigDto
)

data class ApplicationConfigDto(
    val dryRun: Boolean,
    val runOnce: Boolean,
    val wholeTvShow: Boolean,
    val wholeShowSeedingCheck: Boolean,
    val leavingSoon: String, // Duration as string like "14d"
    val exclusionTags: List<String>,
    val mediaDeletion: MediaDeletionConfigDto,
    val tagBasedDeletion: TagDeletionConfigDto,
    val episodeDeletion: EpisodeDeletionConfigDto
)

data class MediaDeletionConfigDto(
    val enabled: Boolean,
    val movieExpiration: Map<Int, String>, // Duration as string
    val seasonExpiration: Map<Int, String>
)

data class TagDeletionConfigDto(
    val enabled: Boolean,
    val minimumFreeDiskPercent: Double,
    val schedules: List<TagScheduleDto>
)

data class TagScheduleDto(
    val tag: String,
    val expiration: String // Duration as string
)

data class EpisodeDeletionConfigDto(
    val enabled: Boolean,
    val tag: String,
    val maxEpisodes: Int,
    val maxAge: String // Duration as string
)

data class FileSystemConfigDto(
    val access: Boolean,
    val validateSeeding: Boolean,
    val leavingSoonDir: String,
    val mediaServerLeavingSoonDir: String?,
    val fromScratch: Boolean,
    val freeSpaceCheckDir: String
)

data class ClientsConfigDto(
    val sonarr: SonarrConfigDto,
    val radarr: RadarrConfigDto,
    val bazarr: BazarrConfigDto,
    val jellyfin: JellyfinConfigDto,
    val emby: EmbyConfigDto,
    val jellyseerr: JellyseerrConfigDto,
    val jellystat: JellystatConfigDto,
    val streamystats: StreamystatsConfigDto
)

data class SonarrConfigDto(
    val enabled: Boolean,
    val url: String,
    val apiKey: String,
    val deleteEmptyShows: Boolean,
    val determineAgeBy: String?, // "MOST_RECENT" or "OLDEST"
    val importExclusions: Boolean
)

data class RadarrConfigDto(
    val enabled: Boolean,
    val url: String,
    val apiKey: String,
    val onlyDeleteFiles: Boolean,
    val determineAgeBy: String?,
    val importExclusions: Boolean
)

data class BazarrConfigDto(
    val enabled: Boolean,
    val url: String,
    val apiKey: String
)

data class JellyfinConfigDto(
    val enabled: Boolean,
    val url: String,
    val apiKey: String,
    val username: String,
    val password: String,
    val delete: Boolean,
    val leavingSoonTv: String,
    val leavingSoonMovies: String,
    val leavingSoonType: String // "MOVIES_AND_TV", "MOVIES", "TV", "NONE"
)

data class EmbyConfigDto(
    val enabled: Boolean,
    val url: String,
    val apiKey: String,
    val username: String,
    val password: String,
    val delete: Boolean,
    val leavingSoonTv: String,
    val leavingSoonMovies: String,
    val leavingSoonType: String
)

data class JellyseerrConfigDto(
    val enabled: Boolean,
    val url: String,
    val apiKey: String,
    val matchServer: Boolean
)

data class JellystatConfigDto(
    val enabled: Boolean,
    val wholeTvShow: Boolean,
    val url: String,
    val apiKey: String
)

data class StreamystatsConfigDto(
    val enabled: Boolean,
    val wholeTvShow: Boolean,
    val url: String,
    val apiKey: String
)

data class ManagementConfigDto(
    val ui: ManagementUiConfigDto
)

data class ManagementUiConfigDto(
    val enabled: Boolean
)

/**
 * Response DTO for connection tests
 */
data class ConnectionTestResult(
    val success: Boolean,
    val message: String,
    val details: String? = null
)

/**
 * Response for batch connection tests
 */
data class ConnectionTestsResult(
    val sonarr: ConnectionTestResult? = null,
    val radarr: ConnectionTestResult? = null,
    val jellyfin: ConnectionTestResult? = null,
    val emby: ConnectionTestResult? = null,
    val jellyseerr: ConnectionTestResult? = null,
    val jellystat: ConnectionTestResult? = null,
    val streamystats: ConnectionTestResult? = null,
    val bazarr: ConnectionTestResult? = null
)
