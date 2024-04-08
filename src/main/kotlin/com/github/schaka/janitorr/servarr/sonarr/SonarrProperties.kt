package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.servarr.ServarrProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.sonarr")
data class SonarrProperties(
        override val enabled: Boolean,
        override val url: String,
        override val apiKey: String,
        val deleteEmptyShows: Boolean = true
) : ServarrProperties