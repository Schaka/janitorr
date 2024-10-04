package com.github.schaka.janitorr.servarr.bazarr

import com.github.schaka.janitorr.servarr.RestClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.bazarr")
data class BazarrProperties(
    override val enabled: Boolean,
    override val url: String,
    override val apiKey: String,
) : RestClientProperties