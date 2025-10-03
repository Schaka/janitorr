package com.github.schaka.janitorr.multitenancy.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for UserRole permissions
 */
class UserRoleTest {
    
    @Test
    fun `should have correct permission levels`() {
        assertEquals(4, UserRole.ADMIN.level)
        assertEquals(3, UserRole.POWER_USER.level)
        assertEquals(2, UserRole.STANDARD_USER.level)
        assertEquals(1, UserRole.READ_ONLY.level)
    }
    
    @Test
    fun `admin should have permission for all roles`() {
        assertTrue(UserRole.ADMIN.hasPermission(UserRole.ADMIN))
        assertTrue(UserRole.ADMIN.hasPermission(UserRole.POWER_USER))
        assertTrue(UserRole.ADMIN.hasPermission(UserRole.STANDARD_USER))
        assertTrue(UserRole.ADMIN.hasPermission(UserRole.READ_ONLY))
    }
    
    @Test
    fun `power user should have limited permissions`() {
        assertFalse(UserRole.POWER_USER.hasPermission(UserRole.ADMIN))
        assertTrue(UserRole.POWER_USER.hasPermission(UserRole.POWER_USER))
        assertTrue(UserRole.POWER_USER.hasPermission(UserRole.STANDARD_USER))
        assertTrue(UserRole.POWER_USER.hasPermission(UserRole.READ_ONLY))
    }
    
    @Test
    fun `standard user should have basic permissions`() {
        assertFalse(UserRole.STANDARD_USER.hasPermission(UserRole.ADMIN))
        assertFalse(UserRole.STANDARD_USER.hasPermission(UserRole.POWER_USER))
        assertTrue(UserRole.STANDARD_USER.hasPermission(UserRole.STANDARD_USER))
        assertTrue(UserRole.STANDARD_USER.hasPermission(UserRole.READ_ONLY))
    }
    
    @Test
    fun `read only should only have read permissions`() {
        assertFalse(UserRole.READ_ONLY.hasPermission(UserRole.ADMIN))
        assertFalse(UserRole.READ_ONLY.hasPermission(UserRole.POWER_USER))
        assertFalse(UserRole.READ_ONLY.hasPermission(UserRole.STANDARD_USER))
        assertTrue(UserRole.READ_ONLY.hasPermission(UserRole.READ_ONLY))
    }
}
