package com.github.schaka.janitorr.seerr

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.jellyseerr")
data class SeerrProperties(
        val enabled: Boolean,
        val url: String,
        val apiKey: String,
        val matchServer: Boolean = false
)
