package com.github.schaka.janitorr.servarr.radarr

import com.fasterxml.jackson.annotation.JsonProperty

// there are some edge cases where trackers don't respond or autobrr adding a torrent that causes hash, indexer and downloadClient to be null
data class RadarrQueueItem(
    val id: Int,
    val movieId: Int,
    val downloadClient: String?,
    @JsonProperty("downloadId")
    var hash: String?,
    val indexer: String?
)