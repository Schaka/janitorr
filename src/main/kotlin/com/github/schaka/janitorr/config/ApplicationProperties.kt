package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration

@ConfigurationProperties(prefix = "application")
data class ApplicationProperties(
        @NestedConfigurationProperty
        val mediaDeletion: MediaDeletion,
        @NestedConfigurationProperty
        val tagBasedDeletion: TagDeletion,
        var dryRun: Boolean = false,
        var leavingSoon: Duration = Duration.ofDays(14),
        var exclusionTag: String = "janitorr_keep"
)