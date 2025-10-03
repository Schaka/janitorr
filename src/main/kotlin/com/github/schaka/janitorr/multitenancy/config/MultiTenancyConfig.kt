package com.github.schaka.janitorr.multitenancy.config

import com.github.schaka.janitorr.multitenancy.repository.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
class MultiTenancyConfig {
    
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
