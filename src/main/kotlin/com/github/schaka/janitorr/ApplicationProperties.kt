package com.github.schaka.janitorr

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "application")
data class ApplicationProperties(
        var dryRun: Boolean = false,
        var leavingSoon: Duration = Duration.ofDays(14),
        var movieExpiration: Map<Int, Duration> = mapOf(
                10 to Duration.ofDays(90),
                20 to Duration.ofDays(120)
                ),
        var seasonExpiration: Map<Int, Duration> = mapOf(
                10 to Duration.ofDays(90),
                20 to Duration.ofDays(120)
        ),
        var exclusionTag: String = "janitorr_keep"
)