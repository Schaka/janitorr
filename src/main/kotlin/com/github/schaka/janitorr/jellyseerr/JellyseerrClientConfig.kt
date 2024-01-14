package com.github.schaka.janitorr.jellyseerr

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.client.RestTemplate

@Configuration
class JellyseerrClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun jellyseerrClient(properties: JellyseerrProperties, mapper: ObjectMapper): JellyseerrClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header("X-Api-Key", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(JellyseerrClient::class.java, properties.url + "/api/v1")
    }

}