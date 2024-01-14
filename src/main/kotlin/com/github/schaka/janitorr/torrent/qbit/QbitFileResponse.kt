package com.github.schaka.janitorr.torrent.qbit

import com.fasterxml.jackson.annotation.JsonProperty

data class QbitFileResponse(
    val availability: Float,
    val index: Int,
    @JsonProperty("is_seed")
    val isSeeding: Boolean,
    val name: String,
    val priority: Long,
    val progress: Float,
    val size: Long
)