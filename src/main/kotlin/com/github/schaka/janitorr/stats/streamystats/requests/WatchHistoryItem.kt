package com.github.schaka.janitorr.stats.streamystats.requests

import com.fasterxml.jackson.annotation.JsonProperty

data class WatchHistoryItem(
    val id: Long,
    val name: String,
    val type: String,
    @JsonProperty("jellyfin_id")
    val jellyfinId: String,
    @JsonProperty("series_name")
    val seriesName: String?,
    @JsonProperty("season_name")
    val seasonName: String?,
)
