package com.github.schaka.janitorr.config

import java.time.Duration

data class MediaDeletion(
        var enabled: Boolean = true,
        var movieExpiration: Map<Int, Duration> = mapOf(
                10 to Duration.ofDays(90),
                20 to Duration.ofDays(120)
        ),
        var seasonExpiration: Map<Int, Duration> = mapOf(
                10 to Duration.ofDays(90),
                20 to Duration.ofDays(120)
        )
)
