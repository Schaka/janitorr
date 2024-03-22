package com.github.schaka.janitorr.config

data class TagDeletion(
        var enabled: Boolean = true,
        var minimumFreeDiskPercent: Double = 20.0,
        var schedules: List<TagDeleteSchedule> = listOf()
)