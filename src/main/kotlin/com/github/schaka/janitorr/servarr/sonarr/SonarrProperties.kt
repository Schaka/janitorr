package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.servarr.HistorySort
import com.github.schaka.janitorr.servarr.ServarrProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.sonarr")
data class SonarrProperties(
    override val enabled: Boolean,
    override val url: String,
    override val apiKey: String,
    override val determineAgeBy: HistorySort? = null,
    override val importExclusions: Boolean = false,
    val deleteEmptyShows: Boolean = true
) : ServarrProperties