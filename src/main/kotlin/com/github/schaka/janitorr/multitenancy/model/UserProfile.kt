package com.github.schaka.janitorr.multitenancy.model

/**
 * User profile containing personalized settings
 */
data class UserProfile(
    val userId: String,
    val displayName: String,
    val preferences: UserPreferences = UserPreferences(),
    val cleanupSettings: UserCleanupSettings = UserCleanupSettings(),
    val notifications: NotificationSettings = NotificationSettings(),
    val mediaAccess: MediaAccessRules = MediaAccessRules(),
    val quotas: ResourceQuotas = ResourceQuotas()
)

/**
 * User-specific preferences
 */
data class UserPreferences(
    val theme: String = "light",
    val language: String = "en",
    val timezone: String = "UTC",
    val dashboardLayout: Map<String, Any> = emptyMap()
)

/**
 * User-specific cleanup configuration
 */
data class UserCleanupSettings(
    val aggressiveness: CleanupAggressiveness = CleanupAggressiveness.MODERATE,
    val allowedMediaTypes: Set<MediaType> = setOf(MediaType.MOVIES, MediaType.TV_SHOWS),
    val preferredSchedule: String? = null,
    val autoCleanupEnabled: Boolean = false
)

enum class CleanupAggressiveness {
    CONSERVATIVE,
    MODERATE,
    AGGRESSIVE
}

enum class MediaType {
    MOVIES,
    TV_SHOWS,
    MUSIC,
    BOOKS
}

/**
 * Notification preferences for users
 */
data class NotificationSettings(
    val enabled: Boolean = true,
    val channels: Set<NotificationChannel> = setOf(NotificationChannel.EMAIL),
    val frequency: NotificationFrequency = NotificationFrequency.IMMEDIATE,
    val eventTypes: Set<NotificationEventType> = NotificationEventType.entries.toSet()
)

enum class NotificationChannel {
    EMAIL,
    WEBHOOK,
    DISCORD,
    SLACK
}

enum class NotificationFrequency {
    IMMEDIATE,
    HOURLY,
    DAILY,
    WEEKLY
}

enum class NotificationEventType {
    CLEANUP_COMPLETED,
    CLEANUP_FAILED,
    MEDIA_DELETED,
    QUOTA_WARNING,
    SYSTEM_ERROR
}

/**
 * Media access rules for granular library access
 */
data class MediaAccessRules(
    val allowedLibraries: Set<String> = emptySet(),
    val deniedLibraries: Set<String> = emptySet(),
    val allowedGenres: Set<String> = emptySet(),
    val minimumQuality: String? = null,
    val allowedLanguages: Set<String> = emptySet()
)

/**
 * Resource quotas and limits per user
 */
data class ResourceQuotas(
    val maxStorageBytes: Long = -1, // -1 means unlimited
    val maxApiCallsPerDay: Int = 10000,
    val maxCleanupOperationsPerDay: Int = 100,
    val currentStorageUsed: Long = 0,
    val currentApiCallsToday: Int = 0,
    val currentCleanupOperationsToday: Int = 0
)
