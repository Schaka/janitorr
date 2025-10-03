package com.github.schaka.janitorr.multitenancy.security

import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.service.UserContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Unit tests for AuthorizationUtils
 */
class AuthorizationUtilsTest {
    
    @AfterEach
    fun cleanup() {
        UserContext.clearContext()
    }
    
    @Test
    fun `requireAuthenticated should throw when not authenticated`() {
        // When/Then
        val exception = assertThrows<ResponseStatusException> {
            AuthorizationUtils.requireAuthenticated()
        }
        assertEquals(HttpStatus.UNAUTHORIZED, exception.statusCode)
        assertTrue(exception.reason?.contains("Authentication required") == true)
    }
    
    @Test
    fun `requireAuthenticated should pass when authenticated`() {
        // Given
        UserContext.setContext("user-123", UserRole.STANDARD_USER)
        
        // When/Then - should not throw
        assertDoesNotThrow {
            AuthorizationUtils.requireAuthenticated()
        }
    }
    
    @Test
    fun `requireAdmin should throw when not authenticated`() {
        // When/Then
        assertThrows<ResponseStatusException> {
            AuthorizationUtils.requireAdmin()
        }
    }
    
    @Test
    fun `requireAdmin should throw when user is not admin`() {
        // Given
        UserContext.setContext("user-123", UserRole.STANDARD_USER)
        
        // When/Then
        val exception = assertThrows<ResponseStatusException> {
            AuthorizationUtils.requireAdmin()
        }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
    }
    
    @Test
    fun `requireAdmin should pass when user is admin`() {
        // Given
        UserContext.setContext("admin-123", UserRole.ADMIN)
        
        // When/Then - should not throw
        assertDoesNotThrow {
            AuthorizationUtils.requireAdmin()
        }
    }
    
    @Test
    fun `requireRole should check role hierarchy correctly`() {
        // Test ADMIN has all permissions
        UserContext.clearContext()
        UserContext.setContext("admin-123", UserRole.ADMIN)
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.ADMIN) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.POWER_USER) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.STANDARD_USER) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.READ_ONLY) }
        
        // Test POWER_USER permissions
        UserContext.clearContext()
        UserContext.setContext("power-123", UserRole.POWER_USER)
        assertThrows<ResponseStatusException> { AuthorizationUtils.requireRole(UserRole.ADMIN) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.POWER_USER) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.STANDARD_USER) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.READ_ONLY) }
        
        // Test STANDARD_USER permissions
        UserContext.clearContext()
        UserContext.setContext("standard-123", UserRole.STANDARD_USER)
        assertThrows<ResponseStatusException> { AuthorizationUtils.requireRole(UserRole.ADMIN) }
        assertThrows<ResponseStatusException> { AuthorizationUtils.requireRole(UserRole.POWER_USER) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.STANDARD_USER) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.READ_ONLY) }
        
        // Test READ_ONLY permissions
        UserContext.clearContext()
        UserContext.setContext("readonly-123", UserRole.READ_ONLY)
        assertThrows<ResponseStatusException> { AuthorizationUtils.requireRole(UserRole.ADMIN) }
        assertThrows<ResponseStatusException> { AuthorizationUtils.requireRole(UserRole.POWER_USER) }
        assertThrows<ResponseStatusException> { AuthorizationUtils.requireRole(UserRole.STANDARD_USER) }
        assertDoesNotThrow { AuthorizationUtils.requireRole(UserRole.READ_ONLY) }
    }
    
    @Test
    fun `requireUserAccess should allow admin to access any user`() {
        // Given
        UserContext.setContext("admin-123", UserRole.ADMIN)
        
        // When/Then - admin can access any user
        assertDoesNotThrow {
            AuthorizationUtils.requireUserAccess("user-456")
        }
    }
    
    @Test
    fun `requireUserAccess should allow user to access themselves`() {
        // Given
        UserContext.setContext("user-123", UserRole.STANDARD_USER)
        
        // When/Then - user can access themselves
        assertDoesNotThrow {
            AuthorizationUtils.requireUserAccess("user-123")
        }
    }
    
    @Test
    fun `requireUserAccess should deny user from accessing others`() {
        // Given
        UserContext.setContext("user-123", UserRole.STANDARD_USER)
        
        // When/Then - user cannot access others
        val exception = assertThrows<ResponseStatusException> {
            AuthorizationUtils.requireUserAccess("user-456")
        }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        assertTrue(exception.reason?.contains("only modify your own") == true)
    }
    
    @Test
    fun `requireUserManagementAccess should only allow admin`() {
        // Test with admin
        UserContext.clearContext()
        UserContext.setContext("admin-123", UserRole.ADMIN)
        assertDoesNotThrow {
            AuthorizationUtils.requireUserManagementAccess()
        }
        
        // Test with non-admin
        UserContext.clearContext()
        UserContext.setContext("user-123", UserRole.POWER_USER)
        assertThrows<ResponseStatusException> {
            AuthorizationUtils.requireUserManagementAccess()
        }
    }
}
