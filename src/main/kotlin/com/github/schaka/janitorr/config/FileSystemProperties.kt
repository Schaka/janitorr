package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "file-system")
data class FileSystemProperties(
        val access: Boolean = false,
        val leavingSoonDir: String = "/data/media/leaving-soon",
        val mediaServerLeavingSoonDir: String?,
        val validateSeeding: Boolean = true,
        val fromScratch: Boolean = true,
        val freeSpaceCheckDir: String = "/"
)