package com.github.schaka.janitorr.external.common

import com.github.schaka.janitorr.external.omdb.OmdbClient
import com.github.schaka.janitorr.external.tmdb.TmdbClient
import com.github.schaka.janitorr.external.trakt.TraktClient
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ExternalDataProperties::class)
@ConditionalOnProperty(prefix = "external-apis", name = ["enabled"], havingValue = "true")
class ExternalApiConfig(
    private val externalDataProperties: ExternalDataProperties
) {

    @Bean
    @ConditionalOnProperty(prefix = "external-apis.tmdb", name = ["enabled"], havingValue = "true")
    fun tmdbClient(): TmdbClient {
        return Feign.builder()
            .encoder(JacksonEncoder())
            .decoder(JacksonDecoder())
            .target(TmdbClient::class.java, externalDataProperties.tmdb.baseUrl)
    }

    @Bean
    @ConditionalOnProperty(prefix = "external-apis.omdb", name = ["enabled"], havingValue = "true")
    fun omdbClient(): OmdbClient {
        return Feign.builder()
            .encoder(JacksonEncoder())
            .decoder(JacksonDecoder())
            .target(OmdbClient::class.java, externalDataProperties.omdb.baseUrl)
    }

    @Bean
    @ConditionalOnProperty(prefix = "external-apis.trakt", name = ["enabled"], havingValue = "true")
    fun traktClient(): TraktClient {
        return Feign.builder()
            .encoder(JacksonEncoder())
            .decoder(JacksonDecoder())
            .requestInterceptor { template ->
                template.header("Content-Type", "application/json")
                template.header("trakt-api-version", "2")
                template.header("trakt-api-key", externalDataProperties.trakt.clientId)
            }
            .target(TraktClient::class.java, externalDataProperties.trakt.baseUrl)
    }
}
