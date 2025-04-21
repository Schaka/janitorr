package com.github.schaka.janitorr.stats.jellystat

import com.github.schaka.janitorr.stats.StatsClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.jellystat")
data class JellystatProperties(
        override val enabled: Boolean,
        override val url: String,
        override val wholeTvShow: Boolean = false,
        val apiKey: String,
) : StatsClientProperties
