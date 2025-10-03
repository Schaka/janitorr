package com.github.schaka.janitorr.ai.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

/**
 * Represents a viewing session for a media item.
 * This data is collected from statistics services (Tautulli, Jellystat, Streamystats)
 * and used for ML model training and inference.
 */
data class ViewingSession(
    val mediaId: String,
    val userId: String,
    val startTime: Instant,
    val endTime: Instant,
    val completionPercentage: Double,
    val device: String,
    val timeOfDay: LocalTime,
    val dayOfWeek: DayOfWeek
)

/**
 * Represents a cleanup decision made by the user or the system.
 * Used for training ML models to learn user preferences.
 */
data class CleanupDecision(
    val mediaId: String,
    val userDecision: Decision,
    val aiRecommendation: Double?,
    val features: Map<String, Double>,
    val timestamp: Instant
)

/**
 * Possible decisions for media cleanup
 */
enum class Decision {
    KEEP,       // Keep the media
    DELETE,     // Delete the media
    ARCHIVE,    // Archive to slower storage
    REVIEW      // Flag for manual review
}
