package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.mediaserver.MediaServerProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.emby")
data class EmbyProperties(
        override val enabled: Boolean,
        override val url: String,
        override val apiKey: String,
        override val username: String,
        override val password: String,
        override val delete: Boolean = true,
        override val leavingSoonTv: String = "Shows (Deleted Soon)",
        override val leavingSoonMovies: String = "Movies (Deleted Soon)"
) : MediaServerProperties