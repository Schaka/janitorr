package com.github.schaka.janitorr.torrent.transmission

data class TransmissionRequest<T>(
    val method: String,
    val arguments: T
)
