package com.github.schaka.janitorr

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "application")
data class ApplicationProperties(
    var dryRun: Boolean = false,
    var leavingSoon: Duration = Duration.ofDays(14),
    var movieExpiration: Duration = Duration.ofDays(90),
    var seasonExpiration: Duration =Duration.ofDays(90),
)