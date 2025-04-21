package com.github.schaka.janitorr.stats.streamystats.requests

import com.fasterxml.jackson.annotation.JsonProperty

data class WatchHistoryEntry(
    val id: Long,
    @JsonProperty("play_duration")
    val playDuration: Int,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("start_time")
    val startTime: String,

)
