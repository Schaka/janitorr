package com.github.schaka.janitorr.jellyseerr

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.jellyseerr")
data class JellyseerrProperties(
        val enabled: Boolean,
        val url: String,
        val apiKey: String,
        val matchServer: Boolean = false
)