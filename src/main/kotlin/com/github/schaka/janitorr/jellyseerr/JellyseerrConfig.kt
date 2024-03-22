package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.config.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class JellyseerrConfig(
    val jellyseerrClient: JellyseerrClient
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun jellyseerrService(jellyseerrProperties: JellyseerrProperties, applicationProperties: ApplicationProperties): JellyseerrService {
        return if(jellyseerrProperties.enabled) JellyseerrRestService(jellyseerrClient, applicationProperties) else JellyseerrNoOpService()
    }

}