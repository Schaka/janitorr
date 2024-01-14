package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.servarr.radarr.RadarrQueueItem

data class RadarrQueueList(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<RadarrQueueItem>
)