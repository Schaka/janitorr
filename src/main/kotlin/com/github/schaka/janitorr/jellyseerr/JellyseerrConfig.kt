package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.servarr.radarr.RadarrProperties
import com.github.schaka.janitorr.servarr.sonarr.SonarrProperties
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
    fun jellyseerrService(
        jellyseerrProperties: JellyseerrProperties,
        sonarrProperties: SonarrProperties,
        radarrProperties: RadarrProperties,
        applicationProperties: ApplicationProperties
    ): JellyseerrService {
        return if (jellyseerrProperties.enabled)
            JellyseerrRestService(jellyseerrClient, jellyseerrProperties, sonarrProperties, radarrProperties, applicationProperties)
        else JellyseerrNoOpService()
    }

}