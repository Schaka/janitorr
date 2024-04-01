package com.github.schaka.janitorr.servarr.sonarr

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.sonarr")
data class SonarrProperties(
        val enabled: Boolean,
        val url: String,
        val apiKey: String,
        val handleEmptyShows: Boolean = true,
        val onlyUnmonitorEmpty: Boolean = false
)