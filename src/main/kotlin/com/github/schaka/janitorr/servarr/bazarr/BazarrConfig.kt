package com.github.schaka.janitorr.servarr.bazarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.DefaultClientProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import feign.Feign
import feign.Request
import feign.jackson3.Jackson3Decoder
import feign.jackson3.Jackson3Encoder
import feign.slf4j.Slf4jLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.databind.json.JsonMapper

@Configuration(proxyBeanMethods = false)
class BazarrConfig(
    val bazarrNoOpService: BazarrNoOpService
) {

    @Bean
    fun bazarrService(
        bazarrProperties: BazarrProperties,
        bazarrClient: BazarrClient,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties
    ): BazarrService {

        if (bazarrProperties.enabled) {
            return BazarrRestService(bazarrClient, applicationProperties, fileSystemProperties, bazarrProperties)
        }

        return bazarrNoOpService
    }

    @Bean
    fun bazarrClient(properties: BazarrProperties, defaults: DefaultClientProperties, mapper: JsonMapper): BazarrClient {
        return Feign.builder()
            .options(Request.Options(defaults.connectTimeout, defaults.readTimeout, true))
            .logLevel(defaults.level)
                .logger(Slf4jLogger())
            .decoder(Jackson3Decoder(mapper))
            .encoder(Jackson3Encoder(mapper))
            .requestInterceptor {
                it.header("X-API-KEY", properties.apiKey)
                it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            }
            .target(BazarrClient::class.java, "${properties.url}/api")
    }
}