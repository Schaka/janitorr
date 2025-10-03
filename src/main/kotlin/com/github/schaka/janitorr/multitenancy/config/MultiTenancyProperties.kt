package com.github.schaka.janitorr.multitenancy.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for multi-tenancy feature.
 */
@ConfigurationProperties(prefix = "multitenancy")
data class MultiTenancyProperties(
    /**
     * Enable/disable multi-tenancy feature
     */
    val enabled: Boolean = false,
    
    /**
     * Authentication configuration
     */
    val auth: AuthConfig = AuthConfig(),
    
    /**
     * Default admin user configuration (created on startup if doesn't exist)
     */
    val defaultAdmin: DefaultAdminConfig = DefaultAdminConfig()
)

data class AuthConfig(
    /**
     * Enable JWT authentication
     */
    val jwtEnabled: Boolean = false,
    
    /**
     * JWT secret key (should be overridden in production)
     */
    val jwtSecret: String = "change-this-secret-key-in-production",
    
    /**
     * JWT token expiration in seconds (default: 24 hours)
     */
    val jwtExpirationSeconds: Long = 86400,
    
    /**
     * Enable OAuth integration
     */
    val oauthEnabled: Boolean = false
)

data class DefaultAdminConfig(
    /**
     * Create default admin user on startup
     */
    val createOnStartup: Boolean = false,
    
    /**
     * Default admin email
     */
    val email: String = "admin@janitorr.local",
    
    /**
     * Default admin password (should be changed immediately)
     */
    val password: String = "change-me-please"
)
