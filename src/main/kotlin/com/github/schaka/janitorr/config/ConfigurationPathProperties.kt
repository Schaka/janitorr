package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "configuration.paths")
data class ConfigurationPathProperties(
    val configFile: String = "/config/application.yml",
    val backupDirectory: String = "/config/backups"
)
