package com.github.schaka.janitorr.multitenancy.service

import com.github.schaka.janitorr.multitenancy.model.UserRole
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for UserContext
 */
class UserContextTest {
    
    @AfterEach
    fun cleanup() {
        UserContext.clearContext()
        TenantContext.clear()
    }
    
    @Test
    fun `should set and get user context`() {
        // When
        UserContext.setContext("user-123", UserRole.ADMIN, "tenant-456")
        
        // Then
        assertEquals("user-123", UserContext.getCurrentUserId())
        assertEquals(UserRole.ADMIN, UserContext.getCurrentUserRole())
        assertEquals("tenant-456", UserContext.getCurrentTenantId())
    }
    
    @Test
    fun `should clear user context`() {
        // Given
        UserContext.setContext("user-123", UserRole.ADMIN)
        assertNotNull(UserContext.getCurrentUserId())
        
        // When
        UserContext.clearContext()
        
        // Then
        assertNull(UserContext.getCurrentUserId())
        assertNull(UserContext.getCurrentUserRole())
        assertNull(UserContext.getCurrentTenantId())
    }
    
    @Test
    fun `should check if user is authenticated`() {
        // When - not authenticated
        assertFalse(UserContext.isAuthenticated())
        
        // When - authenticated
        UserContext.setContext("user-123", UserRole.STANDARD_USER)
        assertTrue(UserContext.isAuthenticated())
    }
    
    @Test
    fun `should check user role permissions`() {
        // Given
        UserContext.setContext("user-123", UserRole.POWER_USER)
        
        // Then
        assertTrue(UserContext.hasRole(UserRole.STANDARD_USER)) // Lower level
        assertTrue(UserContext.hasRole(UserRole.POWER_USER)) // Same level
        assertFalse(UserContext.hasRole(UserRole.ADMIN)) // Higher level
    }
    
    @Test
    fun `should check if user is admin`() {
        // When - not admin
        UserContext.setContext("user-123", UserRole.STANDARD_USER)
        assertFalse(UserContext.isAdmin())
        
        // When - admin
        UserContext.clearContext()
        UserContext.setContext("admin-123", UserRole.ADMIN)
        assertTrue(UserContext.isAdmin())
    }
    
    @Test
    fun `should set and get tenant context`() {
        // When
        TenantContext.setTenantId("tenant-789")
        
        // Then
        assertEquals("tenant-789", TenantContext.getTenantId())
        assertTrue(TenantContext.isSet())
    }
    
    @Test
    fun `should clear tenant context`() {
        // Given
        TenantContext.setTenantId("tenant-789")
        assertTrue(TenantContext.isSet())
        
        // When
        TenantContext.clear()
        
        // Then
        assertNull(TenantContext.getTenantId())
        assertFalse(TenantContext.isSet())
    }
    
    @Test
    fun `should handle null tenant context`() {
        // When
        TenantContext.setTenantId(null)
        
        // Then
        assertNull(TenantContext.getTenantId())
        assertFalse(TenantContext.isSet())
    }
}
