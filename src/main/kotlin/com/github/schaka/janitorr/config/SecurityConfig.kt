package com.github.schaka.janitorr.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring Security configuration for API endpoint protection.
 * 
 * Provides HTTP Basic Authentication for /api/** endpoints when enabled.
 * Health check and actuator endpoints remain publicly accessible.
 * 
 * Enable via application.yml:
 * ```yaml
 * security:
 *   enabled: true
 *   username: your-username
 *   password: your-secure-password
 * ```
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties::class)
@ConditionalOnProperty(prefix = "security", name = ["enabled"], havingValue = "true")
class SecurityConfig(
    private val securityProperties: SecurityProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    }

    init {
        log.info("Security enabled - API endpoints will require authentication")
        if (securityProperties.username == "admin" && securityProperties.password == "admin") {
            log.warn("⚠️  WARNING: Using default credentials! Please change security.username and security.password in production!")
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    // Allow health check endpoints
                    .requestMatchers("/health", "/actuator/health", "/actuator/info").permitAll()
                    // Require authentication for all API endpoints
                    .requestMatchers("/api/**").authenticated()
                    // Allow static resources (Management UI)
                    .requestMatchers("/", "/index.html", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.svg", "/*.ico").permitAll()
                    // Require authentication for everything else
                    .anyRequest().authenticated()
            }
            .httpBasic { }
            .csrf { csrf -> csrf.disable() } // Disable CSRF for API endpoints

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user = User.builder()
            .username(securityProperties.username)
            .password(passwordEncoder().encode(securityProperties.password))
            .roles("ADMIN")
            .build()

        return InMemoryUserDetailsManager(user)
    }
}
