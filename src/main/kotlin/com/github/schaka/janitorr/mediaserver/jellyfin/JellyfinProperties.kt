package com.github.schaka.janitorr.mediaserver.jellyfin

import com.github.schaka.janitorr.mediaserver.MediaServerProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.jellyfin")
data class JellyfinProperties(
        override val enabled: Boolean,
        override val url: String,
        override val apiKey: String,
        override val username: String,
        override val password: String,
        override val delete: Boolean = true
) : MediaServerProperties