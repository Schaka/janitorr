package com.github.schaka.janitorr.stats.streamystats.requests

import com.fasterxml.jackson.annotation.JsonProperty

data class WatchHistoryStatistics(
    @JsonProperty("last_watched")
    val lastWatched: String?,
    @JsonProperty("watch_history")
    val watchHistory: List<WatchHistoryEntry> = listOf(),
)
