package com.github.schaka.janitorr.stats.streamystats.requests


data class WatchHistoryItem(
    val id: String,
    val name: String,
    val type: String,
    val libraryId: String?,
    val seriesId: String?,
    val seasonId: String?,
    val seriesName: String?,
    val seasonName: String?,
)
