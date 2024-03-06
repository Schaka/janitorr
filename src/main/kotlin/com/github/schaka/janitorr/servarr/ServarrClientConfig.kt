package com.github.schaka.janitorr.servarr

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.servarr.radarr.Radarr
import com.github.schaka.janitorr.servarr.radarr.RadarrClient
import com.github.schaka.janitorr.servarr.radarr.RadarrProperties
import com.github.schaka.janitorr.servarr.sonarr.Sonarr
import com.github.schaka.janitorr.servarr.sonarr.SonarrClient
import com.github.schaka.janitorr.servarr.sonarr.SonarrProperties
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.web.client.RestTemplate

@Configuration
class ServarrClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Radarr
    @Bean
    fun radarrRestTemplate(builder: RestTemplateBuilder, properties: RadarrProperties): RestTemplate {
        return builder
            .rootUri("${properties.url}/api/v3")
            .defaultHeader("X-Api-Key", properties.apiKey)
            .build()
    }

    @Sonarr
    @Bean
    fun sonarrRestTemplate(builder: RestTemplateBuilder, properties: SonarrProperties): RestTemplate {
        return builder
            .rootUri("${properties.url}/api/v3")
            .defaultHeader("X-Api-Key", properties.apiKey)
            .build()
    }

    @Bean
    fun radarrClient(properties: RadarrProperties, mapper: ObjectMapper): RadarrClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header("X-Api-Key", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(RadarrClient::class.java, "${properties.url}/api/v3")
    }

    @Bean
    fun sonarrClient(properties: SonarrProperties, mapper: ObjectMapper): SonarrClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header("X-Api-Key", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(SonarrClient::class.java, "${properties.url}/api/v3")
    }

}