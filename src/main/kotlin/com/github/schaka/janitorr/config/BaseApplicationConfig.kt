package com.github.schaka.janitorr.config

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper


@Configuration
class BaseApplicationConfig {

    @Bean
    fun customizer(): JsonMapperBuilderCustomizer {
        return JsonMapperBuilderCustomizer { builder: JsonMapper.Builder ->
            builder.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        }
    }
}