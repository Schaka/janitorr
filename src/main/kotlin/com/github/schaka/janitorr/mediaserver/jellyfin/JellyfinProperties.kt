package com.github.schaka.janitorr.mediaserver.jellyfin

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.jellyfin")
data class JellyfinProperties(
    val url: String,
    val apiKey: String,
    val username: String,
    val password: String
)