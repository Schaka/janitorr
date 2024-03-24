package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Seems to be required for native image to get @NestedConfigurationProperty to work
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value = [ApplicationProperties::class])
class ApplicationConfig {

    @Bean
    fun applicationProperties(applicationProperties: ApplicationProperties): ApplicationProperties {
        return applicationProperties
    }

}