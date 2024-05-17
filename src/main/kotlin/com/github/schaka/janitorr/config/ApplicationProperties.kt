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
        val dryRun: Boolean = false,
        val wholeTvShow: Boolean = false,
        val wholeShowSeedingCheck: Boolean = false,
        val leavingSoon: Duration = Duration.ofDays(14),
        val exclusionTag: String = "janitorr_keep"
)