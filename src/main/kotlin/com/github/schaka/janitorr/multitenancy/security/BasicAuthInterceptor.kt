package com.github.schaka.janitorr.multitenancy.security

import com.github.schaka.janitorr.multitenancy.config.MultiTenancyProperties
import com.github.schaka.janitorr.multitenancy.service.UserContext
import com.github.schaka.janitorr.multitenancy.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*

/**
 * Basic HTTP authentication interceptor for multi-tenancy endpoints.
 * 
 * Provides simple authentication using HTTP Basic Auth until Spring Security
 * is fully implemented. This interceptor:
 * - Extracts credentials from Authorization header
 * - Validates against UserService
 * - Sets UserContext for the request
 * - Can be disabled via configuration
 */
@Component
@ConditionalOnProperty(
    prefix = "multitenancy",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class BasicAuthInterceptor(
    private val userService: UserService,
    private val properties: MultiTenancyProperties
) : HandlerInterceptor {
    
    companion object {
        private val log = LoggerFactory.getLogger(BasicAuthInterceptor::class.java)
        private const val AUTH_HEADER = "Authorization"
        private const val BASIC_PREFIX = "Basic "
    }
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // Skip authentication if not required (for backward compatibility)
        if (!properties.auth.requireAuthentication) {
            log.debug("Authentication not required, skipping")
            return true
        }
        
        val authHeader = request.getHeader(AUTH_HEADER)
        
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            log.warn("Missing or invalid Authorization header from ${request.remoteAddr}")
            sendUnauthorized(response, "Authentication required")
            return false
        }
        
        try {
            val credentials = extractCredentials(authHeader)
            val (email, password) = credentials
            
            val user = userService.findByEmail(email)
            
            if (user == null || !user.enabled || !userService.verifyPassword(user, password)) {
                log.warn("Authentication failed: Invalid credentials")
                log.debug("Auth failure details: email=$email, userExists=${user != null}, enabled=${user?.enabled}, passwordValid=${user?.let { userService.verifyPassword(it, password) }}")
                sendUnauthorized(response, "Invalid credentials")
                return false
            }
            
            // Set user context for the request
            UserContext.setContext(user.id, user.role, user.tenantId)
            log.debug("Authenticated user: ${user.email} with role ${user.role}")
            
            // Update last login time
            try {
                userService.updateLastLogin(user.id)
            } catch (e: Exception) {
                log.warn("Failed to update last login for user ${user.id}: ${e.message}", e)
            }
            
            return true
            
        } catch (e: Exception) {
            log.error("Authentication error", e)
            sendUnauthorized(response, "Authentication failed")
            return false
        }
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // Clear user context after request completes
        UserContext.clearContext()
    }
    
    private fun extractCredentials(authHeader: String): Pair<String, String> {
        val base64Credentials = authHeader.substring(BASIC_PREFIX.length)
        val credentials = String(Base64.getDecoder().decode(base64Credentials))
        val parts = credentials.split(":", limit = 2)
        
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid credentials format")
        }
        
        return Pair(parts[0], parts[1])
    }
    
    private fun sendUnauthorized(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.setHeader("WWW-Authenticate", "Basic realm=\"Janitorr Multi-Tenancy API\", charset=\"UTF-8\"")
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"
        response.writer.write("""{"error": "Unauthorized", "message": "$message"}""")
    }
}
