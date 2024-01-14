package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.servarr.sonarr.SonarQueueItem

data class SonarrQueueList(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<SonarQueueItem>
)