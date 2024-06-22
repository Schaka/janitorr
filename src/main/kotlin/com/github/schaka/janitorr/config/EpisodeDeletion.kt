package com.github.schaka.janitorr.config

import java.time.Duration

data class EpisodeDeletion(
        val enabled: Boolean = false,
        val tag: String = "janitorr_daily",
        val maxEpisodes: Int = 10,
        val maxAge: Duration = Duration.ofDays(30),
)
