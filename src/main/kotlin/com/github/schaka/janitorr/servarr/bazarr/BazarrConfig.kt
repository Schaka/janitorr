package com.github.schaka.janitorr.servarr.bazarr

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
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
        bazarrProperties: BazarrProperties,
        bazarrClient: BazarrClient,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties
    ): BazarrService {

        if (bazarrProperties.enabled) {
            return BazarrRestService(bazarrClient, applicationProperties, fileSystemProperties, bazarrProperties)
        }

        return bazarrNoOpService
    }

    @Bean
    fun bazarrClient(properties: BazarrProperties, mapper: ObjectMapper): BazarrClient {
        return Feign.builder()
            .decoder(JacksonDecoder(mapper))
            .encoder(JacksonEncoder(mapper))
            .requestInterceptor {
                it.header("X-API-KEY", properties.apiKey)
                it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            }
            .target(BazarrClient::class.java, "${properties.url}/api")
    }
}