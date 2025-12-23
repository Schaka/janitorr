package com.github.schaka.janitorr.servarr

import com.github.schaka.janitorr.config.DefaultClientProperties
import com.github.schaka.janitorr.config.jackson.compatibility.Jackson3Decoder
import com.github.schaka.janitorr.config.jackson.compatibility.Jackson3Encoder
import com.github.schaka.janitorr.servarr.radarr.RadarrClient
import com.github.schaka.janitorr.servarr.radarr.RadarrProperties
import com.github.schaka.janitorr.servarr.sonarr.SonarrClient
import com.github.schaka.janitorr.servarr.sonarr.SonarrProperties
import feign.Feign
import feign.Request
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.databind.json.JsonMapper

@Configuration(proxyBeanMethods = false)
class ServarrClientConfig {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun radarrClient(defaults: DefaultClientProperties, properties: RadarrProperties, mapper: JsonMapper): RadarrClient {
        return Feign.builder()
                .options(Request.Options(defaults.connectTimeout, defaults.readTimeout, true))
                .decoder(Jackson3Decoder(mapper))
                .encoder(Jackson3Encoder(mapper))
                .requestInterceptor {
                    it.header("X-Api-Key", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(RadarrClient::class.java, "${properties.url}/api/v3")
    }

    @Bean
    fun sonarrClient(defaults: DefaultClientProperties, properties: SonarrProperties, mapper: JsonMapper): SonarrClient {
        return Feign.builder()
                .options(Request.Options(defaults.connectTimeout, defaults.readTimeout, true))
                .decoder(Jackson3Decoder(mapper))
                .encoder(Jackson3Encoder(mapper))
                .requestInterceptor {
                    it.header("X-Api-Key", properties.apiKey)
                    it.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                }
                .target(SonarrClient::class.java, "${properties.url}/api/v3")
    }

}