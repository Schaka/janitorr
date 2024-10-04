package com.github.schaka.janitorr.servarr.bazarr

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.radarr.RadarrClient
import com.github.schaka.janitorr.servarr.radarr.RadarrProperties
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

/**
 * Only required for native image
 */
@Configuration(proxyBeanMethods = false)
class BazarrConfig(
    val bazarrNoOpService: BazarrNoOpService
) {

    @Bean
    fun bazarrService(
        bazarrPropertiy: BazarrProperties,
        bazarrClient: BazarrClient,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties
    ): BazarrService {

        if (bazarrPropertiy.enabled) {
            return BazarrRestService(bazarrClient, applicationProperties, fileSystemProperties, bazarrPropertiy)
        }

        return bazarrNoOpService
    }

    @Bean
    @ConditionalOnProperty("clients.radarr.enabled", havingValue = "true", matchIfMissing = false)
    fun bazarrClient(properties: BazarrProperties, mapper: ObjectMapper): BazarrClient {
        return Feign.builder()
            .decoder(JacksonDecoder(mapper))
            .encoder(JacksonEncoder(mapper))
            .requestInterceptor {
                it.header("X-Api-Key", properties.apiKey)
                it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            }
            .target(BazarrClient::class.java, "${properties.url}/api")
    }
}