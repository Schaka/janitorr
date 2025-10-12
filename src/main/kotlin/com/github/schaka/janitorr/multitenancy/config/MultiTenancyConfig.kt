package com.github.schaka.janitorr.multitenancy.config

import com.github.schaka.janitorr.multitenancy.repository.*
import com.github.schaka.janitorr.multitenancy.security.BasicAuthInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configuration for multi-tenancy feature.
 * 
 * This feature is disabled by default and can be enabled via configuration.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "multitenancy",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@EnableConfigurationProperties(MultiTenancyProperties::class)
class MultiTenancyConfig(
    @Lazy private val basicAuthInterceptor: BasicAuthInterceptor
) : WebMvcConfigurer {
    
    /**
     * Register authentication interceptor for multi-tenancy endpoints
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(basicAuthInterceptor)
            .addPathPatterns("/api/users/**", "/api/tenants/**")
    }
    
    /**
     * In-memory user repository.
     * Replace with JPA implementation for production use.
     */
    @Bean
    fun userRepository(): UserRepository {
        return InMemoryUserRepository()
    }
    
    /**
     * In-memory user profile repository.
     * Replace with JPA implementation for production use.
     */
    @Bean
    fun userProfileRepository(): UserProfileRepository {
        return InMemoryUserProfileRepository()
    }
    
    /**
     * In-memory tenant repository.
     * Replace with JPA implementation for production use.
     */
    @Bean
    fun tenantRepository(): TenantRepository {
        return InMemoryTenantRepository()
    }
    
    /**
     * In-memory tenant-user repository.
     * Replace with JPA implementation for production use.
     */
    @Bean
    fun tenantUserRepository(): TenantUserRepository {
        return InMemoryTenantUserRepository()
    }
}
