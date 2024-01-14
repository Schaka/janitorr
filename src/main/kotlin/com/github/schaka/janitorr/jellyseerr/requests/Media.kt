package com.github.schaka.janitorr.jellyseerr.requests

data class Media(
    val createdAt: String,
    val id: Int,
    val requests: List<String>?,
    val status: Int,
    val imdbId: String?,
    val tmdbId: Int?,
    val tvdbId: Int?,
    val updatedAt: String
)