package com.github.schaka.janitorr

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "file-system")
data class FileSystemProperties(
    var access: Boolean = false,
)