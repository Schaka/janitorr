package com.github.schaka.janitorr.config

import java.time.Duration

data class MediaDeletion(
        val enabled: Boolean = false,
        val movieExpiration: Map<Int, Duration> = mapOf(
                10 to Duration.ofDays(90),
                20 to Duration.ofDays(120)
        ),
        val seasonExpiration: Map<Int, Duration> = mapOf(
                10 to Duration.ofDays(90),
                20 to Duration.ofDays(120)
        )
)
