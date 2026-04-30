package com.github.schaka.janitorr.stats.janitorrstats.requests

data class JanitorrStatsPlayEvent(
    val userId: String? = null,
    val username: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val playedAt: String,
    val percentComplete: Int = 0,
    val completed: Boolean = false,
    val durationMs: Long = 0,
    val positionMs: Long = 0,
)
