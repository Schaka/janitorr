package com.github.schaka.janitorr.stats.janitorrstats

import com.github.schaka.janitorr.config.DefaultClientProperties
import feign.Feign
import feign.Request
import feign.jackson3.Jackson3Decoder
import feign.jackson3.Jackson3Encoder
import feign.slf4j.Slf4jLogger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.databind.json.JsonMapper

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty("clients.janitorr-stats.enabled", havingValue = "true")
class JanitorrStatsClientConfig {

    @Bean
    fun janitorrStatsClient(properties: JanitorrStatsProperties, defaults: DefaultClientProperties, mapper: JsonMapper): JanitorrStatsClient {
        return Feign.builder()
            .options(Request.Options(defaults.connectTimeout, defaults.readTimeout, true))
            .logLevel(defaults.level)
            .logger(Slf4jLogger())
            .decoder(Jackson3Decoder(mapper))
            .encoder(Jackson3Encoder(mapper))
            .requestInterceptor {
                it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            }
            .target(JanitorrStatsClient::class.java, properties.url)
    }
}
