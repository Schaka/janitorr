package com.github.schaka.janitorr.servarr.sonarr

import com.fasterxml.jackson.annotation.JsonProperty

// there are some edge cases where trackers don't respond or autobrr adding a torrent that causes hash, indexer and downloadClient to be null
data class SonarQueueItem(
    val id: Int,
    val seriesId: Int,
    val episodeId: Int,
    val seasonNumber: Int,
    val downloadClient: String?,
    @JsonProperty("downloadId")
    var hash: String?,
    val indexer: String?,
    val outputPath: String?
)