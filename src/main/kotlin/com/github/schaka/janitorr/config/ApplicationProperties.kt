package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
data class ApplicationProperties(
        @NestedConfigurationProperty
        val mediaDeletion: MediaDeletion,
        @NestedConfigurationProperty
        val tagBasedDeletion: TagDeletion,
        @NestedConfigurationProperty
        val episodeDeletion: EpisodeDeletion,
        val runOnce: Boolean = false,
        val dryRun: Boolean = false,
        val trainingRun: Boolean = false,
        val wholeTvShow: Boolean = false,
        val wholeShowSeedingCheck: Boolean = false,
        val leavingSoon: Duration = Duration.ofDays(14),
        val leavingSoonThresholdOffsetPercent: Int = 0,
        val exclusionTags: List<String> = listOf("janitorr_keep")
)
