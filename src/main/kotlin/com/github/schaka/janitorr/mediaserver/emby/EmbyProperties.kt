package com.github.schaka.janitorr.mediaserver.emby

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.emby")
data class EmbyProperties(
        val url: String,
        val apiKey: String,
        val username: String,
        val password: String
)