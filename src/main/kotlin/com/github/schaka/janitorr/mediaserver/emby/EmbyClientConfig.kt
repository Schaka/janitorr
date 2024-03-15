package com.github.schaka.janitorr.mediaserver.emby

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import feign.Feign
import feign.RequestInterceptor
import feign.RequestTemplate
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Configuration
@ConditionalOnProperty("clients.emby.enabled", havingValue = "true")
class EmbyClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val janitorrClientString = "Emby Client=\"Janitorr\", Device=\"Spring Boot\", DeviceId=\"Janitorr-Device-Id\", Version=\"1.0\""
    }



    @Emby
    @Bean
    fun embyClient(properties: EmbyProperties, mapper: ObjectMapper): MediaServerClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    it.header("X-Emby-Token", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(MediaServerClient::class.java, "${properties.url}/emby")
    }

    @Emby
    @Bean
    fun embyUserClient(properties: EmbyProperties, mapper: ObjectMapper): MediaServerUserClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor(EmbyUserInterceptor(properties))
                .target(MediaServerUserClient::class.java, "${properties.url}/emby")
    }

    private class EmbyUserInterceptor(
            val properties: EmbyProperties
    ) : RequestInterceptor {

        var lastUpdate: LocalDateTime = LocalDateTime.MIN
        var accessToken: String = "invalid-token"

        override fun apply(template: RequestTemplate) {

            if (lastUpdate.plusMinutes(30).isBefore(LocalDateTime.now())) {
                val userInfo = getUserInfo(properties)
                accessToken = userInfo.body?.get("AccessToken").toString()
                lastUpdate = LocalDateTime.now()
                log.info("Logged in to Emby as {} {}", properties.username, accessToken)
            }

            template.header(AUTHORIZATION, janitorrClientString)
            template.header("X-Emby-Token", accessToken)
            template.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        }

        private fun getUserInfo(properties: EmbyProperties): ResponseEntity<Map<*, *>> {
            val login = RestTemplate()
            val headers = HttpHeaders()
            headers.set(AUTHORIZATION, janitorrClientString)
            headers.set(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
            headers.set(ACCEPT, MediaType.ALL_VALUE)

            val content = LinkedMultiValueMap<String, String>()
            content.add("Username", properties.username)
            content.add("Pw", properties.password)
            return login.postForEntity("${properties.url}/emby/Users/AuthenticateByName", HttpEntity(content, headers), Map::class.java)
        }

    }
}