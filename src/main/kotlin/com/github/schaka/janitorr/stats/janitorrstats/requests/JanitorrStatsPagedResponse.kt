package com.github.schaka.janitorr.stats.janitorrstats.requests

data class JanitorrStatsPagedResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val pageSize: Int = 0,
    val totalItems: Long = 0,
    val totalPages: Int = 0,
)
