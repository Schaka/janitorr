package com.github.schaka.janitorr.stats.streamystats

import com.github.schaka.janitorr.stats.StatsClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.streamystats")
data class StreamystatsProperties(
        override val enabled: Boolean,
        override val url: String,
        override val wholeTvShow: Boolean = false,
        val username: String,
        val password: String,
) : StatsClientProperties
