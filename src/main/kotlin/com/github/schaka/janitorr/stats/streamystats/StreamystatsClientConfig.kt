package com.github.schaka.janitorr.stats.streamystats

import com.github.schaka.janitorr.config.jackson.compatibility.Jackson3Decoder
import com.github.schaka.janitorr.config.jackson.compatibility.Jackson3Encoder
import feign.Feign
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.databind.json.JsonMapper

@Configuration(proxyBeanMethods = false)
class StreamystatsClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

        // TODO: make this dynamic
        private val serverId = 1
    }

    @Bean
    fun streamystatsClient(properties: StreamystatsProperties, mapper: JsonMapper): StreamystatsClient {
        return Feign.builder()
                .decoder(Jackson3Decoder(mapper))
                .encoder(Jackson3Encoder(mapper))
                .requestInterceptor {
                    it.header(AUTHORIZATION, properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    it.query("serverId", "$serverId")
                }
                .target(StreamystatsClient::class.java, properties.url + "/api")
    }
}