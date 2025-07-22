package com.github.schaka.janitorr.stats.streamystats.requests

data class WatchHistoryEntry(
    val id: Long,
    val watchDuration: Int,
    val user: WatchHistoryUser,
    val watchDate: String,
    val playMethod: String,
    val deviceName: String,
    val clientName: String
)
