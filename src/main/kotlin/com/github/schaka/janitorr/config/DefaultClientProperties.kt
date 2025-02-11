package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "clients.default")
class DefaultClientProperties(
    val connectTimeout: Duration = Duration.ofSeconds(60),
    val readTimeout: Duration = Duration.ofSeconds(60),
)
