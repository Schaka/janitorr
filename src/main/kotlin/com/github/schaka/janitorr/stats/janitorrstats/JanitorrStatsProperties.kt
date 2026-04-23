package com.github.schaka.janitorr.stats.janitorrstats

import com.github.schaka.janitorr.stats.StatsClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.janitorr-stats")
data class JanitorrStatsProperties(
        override val enabled: Boolean = false,
        override val url: String = "",
        override val wholeTvShow: Boolean = false,
) : StatsClientProperties
