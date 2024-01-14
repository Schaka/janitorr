package com.github.schaka.janitorr.jellyseerr.requests

data class RequestResponse(
    val createdAt: String,
    val id: Int,
    val is4k: Boolean,
    val media: Media,
    val modifiedBy: ModifiedBy?,
    val profileId: Int,
    val requestedBy: ModifiedBy,
    val rootFolder: String,
    val serverId: Int,
    val status: Int,
    val updatedAt: String,
    val seasons: List<RequestSeason>?
)