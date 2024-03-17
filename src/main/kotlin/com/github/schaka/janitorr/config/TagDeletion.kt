package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties
data class TagDeletion(
        var enabled: Boolean = true,
        var minimumFreeDiskPercent: Double = 20.0,
        var schedules: List<TagDeleteSchedule> = listOf()
)