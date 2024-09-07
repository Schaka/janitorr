package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "file-system")
data class FileSystemProperties(
        val leavingSoonDir: String?,
        val mediaServerLeavingSoonDir: String?,
        val access: Boolean = false,
        val validateSeeding: Boolean = true,
        val fromScratch: Boolean = true,
        val freeSpaceCheckDir: String = "/"
)