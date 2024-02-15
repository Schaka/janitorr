package com.github.schaka.janitorr.jellyfin

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Configuration
@ConditionalOnProperty("clients.jellyfin.enabled", havingValue = "true")
class JellyfinClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Jellyfin
    @Bean
    fun jellyfinRestTemplate(builder: RestTemplateBuilder, properties: JellyfinProperties): RestTemplate {
        return builder
            .rootUri("${properties.url}/")
            .defaultHeader(AUTHORIZATION, "MediaBrowser Token=\"${properties.apiKey}\", Client=\"Janitorr\", Version=\"1.0\"")
            .build()
    }

    @Bean
    fun jellyfinClient(properties: JellyfinProperties, mapper: ObjectMapper): JellyfinClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header(AUTHORIZATION, "MediaBrowser Token=\"${properties.apiKey}\", Client=\"Janitorr\", Device=\"Spring Boot\", DeviceId=\"Janitorr-Device-Id\", Version=\"1.0\"")
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(JellyfinClient::class.java, properties.url)
    }

    @Bean
    fun jellyfinUserClient(properties: JellyfinProperties, mapper: ObjectMapper): JellyfinUserClient {
        val userInfo = getUserInfo(properties)
        val accessToken = userInfo.body?.get("AccessToken")

        log.info("Logged in to Jellyfin as {} {}", properties.username, accessToken)

        return Feign.builder()
            .decoder(JacksonDecoder(mapper))
            .encoder(JacksonEncoder(mapper))
            .requestInterceptor {
                it.header(AUTHORIZATION, "MediaBrowser Token=\"${accessToken}\", Client=\"Janitorr\", Device=\"Spring Boot\", DeviceId=\"Janitorr-Device-Id\", Version=\"1.0\"")
                it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            }
            .target(JellyfinUserClient::class.java, properties.url)
    }

    private fun getUserInfo(properties: JellyfinProperties): ResponseEntity<Map<*, *>> {
        val login = RestTemplate()
        val headers = HttpHeaders()
        headers.set(AUTHORIZATION, "MediaBrowser Client=\"Janitorr\", Device=\"Spring Boot\", DeviceId=\"Janitorr-Device-Id\", Version=\"1.0\"")
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        val content = object {
            val Username = properties.username
            val Pw = properties.password
        }
        return login.postForEntity("${properties.url}/Users/AuthenticateByName", HttpEntity(content, headers), Map::class.java)
    }

}