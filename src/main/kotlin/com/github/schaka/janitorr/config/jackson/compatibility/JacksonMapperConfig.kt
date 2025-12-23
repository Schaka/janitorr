package com.github.schaka.janitorr.config.jackson.compatibility

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature

@Configuration
class JacksonMapperConfig {

    @Bean
    fun jsonMapperCustomizer() = JsonMapperBuilderCustomizer { builder ->
        builder
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    }
}