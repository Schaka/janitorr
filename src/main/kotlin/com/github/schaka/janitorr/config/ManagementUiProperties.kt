package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "management.ui")
data class ManagementUiProperties(
    val enabled: Boolean = true
)
