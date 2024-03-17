package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "application")
data class ApplicationProperties(
        var dryRun: Boolean = false,
        var leavingSoon: Duration = Duration.ofDays(14),
        @NestedConfigurationProperty
        var mediaDeletion: MediaDeletion,
        @NestedConfigurationProperty
        var tagBasedDeletion: TagDeletion,
        var exclusionTag: String = "janitorr_keep"
)