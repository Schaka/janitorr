package com.github.schaka.janitorr.jellyseerr.requests

data class ModifiedBy(
        val avatar: String,
        val createdAt: String,
        val email: String,
        val id: Int,
        val permissions: Int,
        val requestCount: Int,
        val updatedAt: String,
        val userType: Int,
        var displayName: String,
        val username: String?,
        val plexUsername: String?,
        val jellyfinUsername: String?,
)