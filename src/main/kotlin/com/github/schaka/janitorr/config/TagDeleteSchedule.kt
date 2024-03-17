package com.github.schaka.janitorr.config

import java.time.Duration

data class TagDeleteSchedule(
        val tag: String,
        val expiration: Duration
)
