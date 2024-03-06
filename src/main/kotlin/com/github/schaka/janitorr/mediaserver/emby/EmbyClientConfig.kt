package com.github.schaka.janitorr.mediaserver.emby

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Configuration
@ConditionalOnProperty("clients.emby.enabled", havingValue = "true")
class EmbyClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val janitorrClientString = "Client=\"Janitorr\", Device=\"Spring Boot\", DeviceId=\"Janitorr-Device-Id\", Version=\"1.0\""
    }

    @Bean
    fun embyClient(properties: EmbyProperties, mapper: ObjectMapper): EmbyClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header(AUTHORIZATION, "MediaBrowser Token=\"${properties.apiKey}\", ${janitorrClientString}")
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(EmbyClient::class.java, properties.url)
    }
}