package com.github.schaka.janitorr.config

data class TagDeletion(
        val enabled: Boolean = false,
        val minimumFreeDiskPercent: Double = 20.0,
        val schedules: List<TagDeleteSchedule> = listOf()
)