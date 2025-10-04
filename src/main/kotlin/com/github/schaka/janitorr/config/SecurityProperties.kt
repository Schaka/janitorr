package com.github.schaka.janitorr.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for API security.
 * 
 * Controls HTTP Basic Authentication for API endpoints.
 */
@ConfigurationProperties(prefix = "security")
data class SecurityProperties(
    /**
     * Enable or disable HTTP Basic Authentication.
     * Default: false (disabled for backward compatibility)
     */
    val enabled: Boolean = false,
    
    /**
     * Username for HTTP Basic Authentication.
     * Default: "admin"
     */
    val username: String = "admin",
    
    /**
     * Password for HTTP Basic Authentication.
     * IMPORTANT: Change this in production!
     * Default: "admin"
     */
    val password: String = "admin"
)
