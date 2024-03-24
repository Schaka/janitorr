package com.github.schaka.janitorr.jellystat

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.jellystat")
data class JellystatProperties(
        val enabled: Boolean,
        val url: String,
        val apiKey: String
)
