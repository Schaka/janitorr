package com.github.schaka.janitorr.mediaserver.jellyfin.library

data class AddPathRequest(
    val Name: String,
    val Path: String,
    val PathInfo: PathInfo? = null
)