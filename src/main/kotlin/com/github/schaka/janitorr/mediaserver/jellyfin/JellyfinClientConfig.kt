package com.github.schaka.janitorr.mediaserver.jellyfin

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import feign.Feign
import feign.RequestInterceptor
import feign.RequestTemplate
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.slf4j.LoggerFactory
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
import java.time.LocalDateTime

@Configuration(proxyBeanMethods = false)
class JellyfinClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val janitorrClientString = "Client=\"Janitorr\", Device=\"Spring Boot\", DeviceId=\"Janitorr-Device-Id\", Version=\"1.0\""
    }

    @Jellyfin
    @Bean
    fun jellyfinClient(properties: JellyfinProperties, mapper: ObjectMapper): MediaServerClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header(AUTHORIZATION, "MediaBrowser Token=\"${properties.apiKey}\", $janitorrClientString")
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(MediaServerClient::class.java, properties.url)
    }

    @Jellyfin
    @Bean
    fun jellyfinUserClient(properties: JellyfinProperties, mapper: ObjectMapper): MediaServerUserClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor(JellyfinUserInterceptor(properties))
                .target(MediaServerUserClient::class.java, properties.url)
    }

    private class JellyfinUserInterceptor(
            val properties: JellyfinProperties
    ) : RequestInterceptor {

        var lastUpdate: LocalDateTime = LocalDateTime.MIN
        var accessToken: String = "invalid-token"

        override fun apply(template: RequestTemplate) {

            if (lastUpdate.plusMinutes(30).isBefore(LocalDateTime.now())) {
                val userInfo = getUserInfo(properties)
                accessToken = userInfo.body?.get("AccessToken").toString()
                lastUpdate = LocalDateTime.now()
                log.info("Logged in to Jellyfin as {} {}", properties.username, accessToken)
            }

            template.header(AUTHORIZATION, "MediaBrowser Token=\"${accessToken}\", $janitorrClientString}")
            template.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        }

        private fun getUserInfo(properties: JellyfinProperties): ResponseEntity<Map<*, *>> {
            val login = RestTemplate()
            val headers = HttpHeaders()
            headers.set(AUTHORIZATION, "MediaBrowser , $janitorrClientString")
            headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE)

            val content = LinkedMultiValueMap<String, String>()
            content.add("Username", properties.username)
            content.add("Pw", properties.password)

            return login.postForEntity("${properties.url}/Users/AuthenticateByName", HttpEntity(content, headers), Map::class.java)
        }

    }

}