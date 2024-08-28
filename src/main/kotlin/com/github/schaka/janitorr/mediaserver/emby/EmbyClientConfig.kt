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

@Configuration(proxyBeanMethods = false)
class EmbyClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val janitorrClientString = "Emby Client=\"Janitorr\", Device=\"Spring Boot\", DeviceId=\"b53a1d79-8f38-420c-bd65-1312b5a8ba39\", Version=\"1.0\""

        private val headerMap = mapOf(
            "X-Emby-Client" to "Janitorr",
            "X-Emby-Device-Name" to "Spring Boot",
            "X-Emby-Device-Id" to "b53a1d79-8f38-420c-bd65-1312b5a8ba39",
            "X-Emby-Client-Version" to "1.0",
        )
    }


    @Emby
    @Bean
    fun embyClient(properties: EmbyProperties, mapper: ObjectMapper): EmbyMediaServerClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor {
                    headerMap.map { e -> it.header(e.key, e.value) }
                    it.header("X-Emby-Token", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(EmbyMediaServerClient::class.java, "${properties.url}/emby")
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

            headerMap.map { e -> template.header(e.key, e.value) }
            template.header(AUTHORIZATION, janitorrClientString)
            template.header("X-Emby-Token", accessToken)
            template.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        }

        private fun getUserInfo(properties: EmbyProperties): ResponseEntity<Map<*, *>> {
            val login = RestTemplate()
            val headers = HttpHeaders()
            headerMap.map { e -> headers.set(e.key, e.value) }
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