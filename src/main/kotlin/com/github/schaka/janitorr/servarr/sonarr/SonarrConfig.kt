package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.ServarrService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class SonarrConfig(
    //val sonarrRestService: SonarrRestService,
    val sonarrNoOpService: SonarrNoOpService
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    @Sonarr
    fun sonarrService(
        sonarrProperties: SonarrProperties,
        sonarrClient: SonarrClient,
        filesystemProperties: FileSystemProperties,
        applicationProperties: ApplicationProperties
    ): ServarrService {

        if (sonarrProperties.enabled) {
            //return sonarrRestService
            return SonarrRestService(sonarrClient, filesystemProperties, applicationProperties, sonarrProperties)
        }

        return sonarrNoOpService
    }
}