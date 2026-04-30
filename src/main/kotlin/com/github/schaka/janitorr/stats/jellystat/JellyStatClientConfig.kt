package com.github.schaka.janitorr.stats.jellystat

import com.github.schaka.janitorr.config.DefaultClientProperties
import feign.Feign
import feign.Request
import feign.jackson3.Jackson3Decoder
import feign.jackson3.Jackson3Encoder
import feign.slf4j.Slf4jLogger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.databind.json.JsonMapper

@Configuration(proxyBeanMethods = false)
class JellyStatClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun jellystatClient(properties: JellystatProperties, defaults: DefaultClientProperties, mapper: JsonMapper): JellystatClient {
        return Feign.builder()
            .options(Request.Options(defaults.connectTimeout, defaults.readTimeout, true))
                .logLevel(defaults.level)
                .logger(Slf4jLogger())
                .decoder(Jackson3Decoder(mapper))
                .encoder(Jackson3Encoder(mapper))
                .requestInterceptor {
                    it.header("x-api-token", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(JellystatClient::class.java, properties.url + "/api")
    }
}