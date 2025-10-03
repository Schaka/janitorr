package com.github.schaka.janitorr.multitenancy.security

import com.github.schaka.janitorr.multitenancy.config.AuthConfig
import com.github.schaka.janitorr.multitenancy.config.DefaultAdminConfig
import com.github.schaka.janitorr.multitenancy.config.MultiTenancyProperties
import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.service.UserContext
import com.github.schaka.janitorr.multitenancy.service.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/**
 * Unit tests for BasicAuthInterceptor
 */
class BasicAuthInterceptorTest {
    
    private lateinit var userService: UserService
    private lateinit var properties: MultiTenancyProperties
    private lateinit var interceptor: BasicAuthInterceptor
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var responseWriter: StringWriter
    
    @BeforeEach
    fun setup() {
        userService = mockk()
        request = mockk()
        response = mockk()
        responseWriter = StringWriter()
        
        every { response.writer } returns PrintWriter(responseWriter)
        every { response.setHeader(any(), any()) } returns Unit
        every { response.status = any() } returns Unit
        every { response.contentType = any() } returns Unit
        every { request.remoteAddr } returns "127.0.0.1"
    }
    
    @AfterEach
    fun cleanup() {
        UserContext.clearContext()
    }
    
    @Test
    fun `should bypass authentication when not required`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = false)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertTrue(result)
        assertFalse(UserContext.isAuthenticated())
    }
    
    @Test
    fun `should reject request without authorization header`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        every { request.getHeader("Authorization") } returns null
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertFalse(result)
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
        verify { response.setHeader("WWW-Authenticate", any()) }
    }
    
    @Test
    fun `should reject request with invalid authorization header format`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        every { request.getHeader("Authorization") } returns "Bearer some-token"
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertFalse(result)
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
    }
    
    @Test
    fun `should reject request for non-existent user`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        
        val credentials = Base64.getEncoder().encodeToString("test@example.com:password".toByteArray())
        every { request.getHeader("Authorization") } returns "Basic $credentials"
        every { userService.findByEmail("test@example.com") } returns null
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertFalse(result)
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
        assertFalse(UserContext.isAuthenticated())
    }
    
    @Test
    fun `should reject request for disabled user`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        
        val credentials = Base64.getEncoder().encodeToString("test@example.com:password".toByteArray())
        every { request.getHeader("Authorization") } returns "Basic $credentials"
        
        val disabledUser = mockk<com.github.schaka.janitorr.multitenancy.model.User>()
        every { disabledUser.enabled } returns false
        every { disabledUser.email } returns "test@example.com"
        every { userService.findByEmail("test@example.com") } returns disabledUser
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertFalse(result)
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
        assertFalse(UserContext.isAuthenticated())
    }
    
    @Test
    fun `should reject request with wrong password`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        
        val credentials = Base64.getEncoder().encodeToString("test@example.com:wrongpassword".toByteArray())
        every { request.getHeader("Authorization") } returns "Basic $credentials"
        
        val user = mockk<com.github.schaka.janitorr.multitenancy.model.User>()
        every { user.enabled } returns true
        every { user.email } returns "test@example.com"
        every { userService.findByEmail("test@example.com") } returns user
        every { userService.verifyPassword(user, "wrongpassword") } returns false
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertFalse(result)
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
        assertFalse(UserContext.isAuthenticated())
    }
    
    @Test
    fun `should accept valid credentials and set user context`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        
        val credentials = Base64.getEncoder().encodeToString("admin@example.com:password123".toByteArray())
        every { request.getHeader("Authorization") } returns "Basic $credentials"
        
        val user = mockk<com.github.schaka.janitorr.multitenancy.model.User>()
        every { user.id } returns "user-123"
        every { user.email } returns "admin@example.com"
        every { user.enabled } returns true
        every { user.role } returns UserRole.ADMIN
        every { user.tenantId } returns "tenant-456"
        every { userService.findByEmail("admin@example.com") } returns user
        every { userService.verifyPassword(user, "password123") } returns true
        every { userService.updateLastLogin("user-123") } returns Unit
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertTrue(result)
        assertTrue(UserContext.isAuthenticated())
        assertEquals("user-123", UserContext.getCurrentUserId())
        assertEquals(UserRole.ADMIN, UserContext.getCurrentUserRole())
        assertEquals("tenant-456", UserContext.getCurrentTenantId())
        verify { userService.updateLastLogin("user-123") }
    }
    
    @Test
    fun `afterCompletion should clear user context`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = false)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        UserContext.setContext("user-123", UserRole.ADMIN)
        
        // When
        interceptor.afterCompletion(request, response, Any(), null)
        
        // Then
        assertFalse(UserContext.isAuthenticated())
        assertNull(UserContext.getCurrentUserId())
    }
    
    @Test
    fun `should handle malformed base64 credentials`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        
        every { request.getHeader("Authorization") } returns "Basic invalid-base64!!!"
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertFalse(result)
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
    }
    
    @Test
    fun `should handle credentials without colon separator`() {
        // Given
        properties = MultiTenancyProperties(
            enabled = true,
            auth = AuthConfig(requireAuthentication = true)
        )
        interceptor = BasicAuthInterceptor(userService, properties)
        
        val credentials = Base64.getEncoder().encodeToString("userpassword".toByteArray())
        every { request.getHeader("Authorization") } returns "Basic $credentials"
        
        // When
        val result = interceptor.preHandle(request, response, Any())
        
        // Then
        assertFalse(result)
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
    }
}
