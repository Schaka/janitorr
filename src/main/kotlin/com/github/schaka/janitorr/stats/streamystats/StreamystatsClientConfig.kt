package com.github.schaka.janitorr.stats.streamystats

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Configuration(proxyBeanMethods = false)
class StreamystatsClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun streamystatsClient(properties: StreamystatsProperties, mapper: ObjectMapper): StreamystatsClient {
        return Feign.builder()
                .decoder(JacksonDecoder(mapper))
                .encoder(JacksonEncoder(mapper))
                .requestInterceptor(StreamystatsUserinterceptor(properties))
                .target(StreamystatsClient::class.java, properties.url + "/api/servers/1") // TODO: make this dynamic
    }

    private class StreamystatsUserinterceptor(
        val properties: StreamystatsProperties,
    ) : RequestInterceptor {

        var lastUpdate: LocalDateTime = LocalDateTime.MIN
        var accessToken: String = "invalid-token"

        override fun apply(template: RequestTemplate) {

            if (lastUpdate.plusMinutes(30).isBefore(LocalDateTime.now())) {
                val userInfo = getUserInfo(properties)
                accessToken = (userInfo.headers["Set-Cookie"]
                    ?.flatMap { it.split(";") }
                    ?.firstOrNull { it.contains("streamystats-token") }
                    ?: throw IllegalStateException("Header missing streamystats-token"))
                    .split("=")[1]

                lastUpdate = LocalDateTime.now()
                log.info("Logged in to Streamystats as {} {}", properties.username, accessToken)
            }

            template.header(AUTHORIZATION, "Bearer $accessToken")
            //template.header(HttpHeaders.COOKIE, "$accessToken")
            template.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        }

        private fun getUserInfo(properties: StreamystatsProperties): ResponseEntity<String> {
            val login = RestTemplate()
            val headers = HttpHeaders()
            headers.set(CONTENT_TYPE, "text/plain;charset=UTF-8")
            headers.set("Next-Action", "f8be693f8ad21105acbf6b03f935735a81ceacbd") // doesn't work without this

            // TODO: dynamic serverId
            val loginInfo = """
                [{
                  "username": "${properties.username}",
                  "password": "${properties.password}",
                  "serverId": 1
                }]
            """.trimIndent()

            return login.exchange("${properties.url}/servers/1/login", HttpMethod.POST, HttpEntity(loginInfo, headers), String::class.java)
        }

    }
}