package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "file-system")
data class FileSystemProperties(
        var leavingSoonDir: String?,
        var access: Boolean = false,
        var fromScratch: Boolean = true
)