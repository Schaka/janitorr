package com.github.schaka.janitorr.notifications

import java.time.LocalDateTime

/**
 * Represents different types of notification events in the system
 */
enum class NotificationEventType {
    CLEANUP_COMPLETED,
    CLEANUP_ERROR,
    SYSTEM_STATUS_CHANGE,
    DISK_SPACE_WARNING,
    DAILY_REPORT,
    WEEKLY_REPORT
}

/**
 * Represents a notification event to be sent through various channels
 */
data class NotificationEvent(
    val type: NotificationEventType,
    val title: String,
    val message: String,
    val details: Map<String, Any> = emptyMap(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Statistics for a cleanup operation
 */
data class CleanupStats(
    val cleanupType: String,
    val filesDeleted: Int = 0,
    val spaceFreeGB: Double = 0.0,
    val dryRun: Boolean = true,
    val errors: List<String> = emptyList()
)
