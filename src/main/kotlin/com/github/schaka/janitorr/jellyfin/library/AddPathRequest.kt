package com.github.schaka.janitorr.jellyfin.library

data class AddPathRequest(
    val Name: String,
    val Path: String,
    val PathInfo: PathInfo? = null
)