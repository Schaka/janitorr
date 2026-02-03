package com.github.schaka.janitorr.servarr.sonarr.episodes

data class MonitoringRequest(
    val episodeIds: List<Int>,
    val monitored: Boolean = false
)
