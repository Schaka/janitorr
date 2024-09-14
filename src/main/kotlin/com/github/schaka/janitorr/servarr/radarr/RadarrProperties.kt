package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.servarr.HistorySort
import com.github.schaka.janitorr.servarr.ServarrProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.radarr")
data class RadarrProperties(
    override val enabled: Boolean,
    override val url: String,
    override val apiKey: String,
    override val determineAgeBy: HistorySort? = null,
) : ServarrProperties