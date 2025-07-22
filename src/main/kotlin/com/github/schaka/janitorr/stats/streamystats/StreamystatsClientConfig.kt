package com.github.schaka.janitorr.stats.streamystats

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Configuration(proxyBeanMethods = false)
class StreamystatsClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

        // TODO: make this dynamic
        private val serverId = 1
    }

    @Bean
    fun streamystatsClient(properties: StreamystatsProperties, mapper: ObjectMapper): StreamystatsClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header(AUTHORIZATION, properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    it.query("serverId", "$serverId")
                }
                .target(StreamystatsClient::class.java, properties.url + "/api")
    }
}