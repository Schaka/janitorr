package com.github.schaka.janitorr.multitenancy.service

import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.repository.InMemoryUserProfileRepository
import com.github.schaka.janitorr.multitenancy.repository.InMemoryUserRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for UserService
 */
class UserServiceTest {
    
    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var userProfileRepository: InMemoryUserProfileRepository
    private lateinit var userService: UserService
    
    @BeforeEach
    fun setup() {
        userRepository = InMemoryUserRepository()
        userProfileRepository = InMemoryUserProfileRepository()
        userService = UserService(userRepository, userProfileRepository)
    }
    
    @Test
    fun `should create user successfully`() {
        // When
        val user = userService.createUser(
            email = "test@example.com",
            password = "password123",
            role = UserRole.STANDARD_USER
        )
        
        // Then
        assertNotNull(user.id)
        assertEquals("test@example.com", user.email)
        assertEquals(UserRole.STANDARD_USER, user.role)
        assertTrue(user.enabled)
    }
    
    @Test
    fun `should create default profile when creating user`() {
        // When
        val user = userService.createUser(
            email = "test@example.com",
            password = "password123",
            role = UserRole.STANDARD_USER
        )
        
        // Then
        val profile = userService.getUserProfile(user.id)
        assertNotNull(profile)
        assertEquals(user.id, profile?.userId)
        assertEquals("test", profile?.displayName) // Default from email
    }
    
    @Test
    fun `should throw exception when creating duplicate email`() {
        // Given
        userService.createUser(
            email = "test@example.com",
            password = "password123",
            role = UserRole.ADMIN
        )
        
        // When/Then
        assertThrows<IllegalArgumentException> {
            userService.createUser(
                email = "test@example.com",
                password = "different",
                role = UserRole.STANDARD_USER
            )
        }
    }
    
    @Test
    fun `should find user by email`() {
        // Given
        val created = userService.createUser(
            email = "find@example.com",
            password = "password",
            role = UserRole.POWER_USER
        )
        
        // When
        val found = userService.findByEmail("find@example.com")
        
        // Then
        assertNotNull(found)
        assertEquals(created.id, found?.id)
        assertEquals(created.email, found?.email)
    }
    
    @Test
    fun `should find user by id`() {
        // Given
        val created = userService.createUser(
            email = "findid@example.com",
            password = "password",
            role = UserRole.ADMIN
        )
        
        // When
        val found = userService.findById(created.id)
        
        // Then
        assertNotNull(found)
        assertEquals(created.id, found?.id)
    }
    
    @Test
    fun `should verify correct password`() {
        // Given
        val user = userService.createUser(
            email = "auth@example.com",
            password = "correctpassword",
            role = UserRole.STANDARD_USER
        )
        
        // When/Then
        assertTrue(userService.verifyPassword(user, "correctpassword"))
        assertFalse(userService.verifyPassword(user, "wrongpassword"))
    }
    
    @Test
    fun `should update user role`() {
        // Given
        val user = userService.createUser(
            email = "role@example.com",
            password = "password",
            role = UserRole.STANDARD_USER
        )
        
        // When
        val success = userService.updateRole(user.id, UserRole.POWER_USER)
        
        // Then
        assertTrue(success)
        val updated = userService.findById(user.id)
        assertEquals(UserRole.POWER_USER, updated?.role)
    }
    
    @Test
    fun `should update user password`() {
        // Given
        val user = userService.createUser(
            email = "password@example.com",
            password = "oldpassword",
            role = UserRole.STANDARD_USER
        )
        
        // When
        val success = userService.updatePassword(user.id, "newpassword")
        
        // Then
        assertTrue(success)
        val updated = userService.findById(user.id)!!
        assertTrue(userService.verifyPassword(updated, "newpassword"))
        assertFalse(userService.verifyPassword(updated, "oldpassword"))
    }
    
    @Test
    fun `should enable and disable user`() {
        // Given
        val user = userService.createUser(
            email = "enabled@example.com",
            password = "password",
            role = UserRole.STANDARD_USER
        )
        assertTrue(user.enabled)
        
        // When - disable
        userService.setUserEnabled(user.id, false)
        
        // Then
        var updated = userService.findById(user.id)
        assertFalse(updated?.enabled ?: true)
        
        // When - enable again
        userService.setUserEnabled(user.id, true)
        
        // Then
        updated = userService.findById(user.id)
        assertTrue(updated?.enabled ?: false)
    }
    
    @Test
    fun `should delete user and profile`() {
        // Given
        val user = userService.createUser(
            email = "delete@example.com",
            password = "password",
            role = UserRole.STANDARD_USER
        )
        assertNotNull(userService.findById(user.id))
        assertNotNull(userService.getUserProfile(user.id))
        
        // When
        val success = userService.deleteUser(user.id)
        
        // Then
        assertTrue(success)
        assertNull(userService.findById(user.id))
        assertNull(userService.getUserProfile(user.id))
    }
    
    @Test
    fun `should get all users`() {
        // Given
        userService.createUser("user1@example.com", "password", UserRole.ADMIN)
        userService.createUser("user2@example.com", "password", UserRole.POWER_USER)
        userService.createUser("user3@example.com", "password", UserRole.STANDARD_USER)
        
        // When
        val users = userService.getAllUsers()
        
        // Then
        assertEquals(3, users.size)
    }
    
    @Test
    fun `should filter users by tenant`() {
        // Given
        val tenant1 = "tenant-1"
        val tenant2 = "tenant-2"
        
        userService.createUser("user1@example.com", "password", UserRole.ADMIN, tenant1)
        userService.createUser("user2@example.com", "password", UserRole.ADMIN, tenant1)
        userService.createUser("user3@example.com", "password", UserRole.ADMIN, tenant2)
        
        // When
        val tenant1Users = userService.getUsersByTenant(tenant1)
        val tenant2Users = userService.getUsersByTenant(tenant2)
        
        // Then
        assertEquals(2, tenant1Users.size)
        assertEquals(1, tenant2Users.size)
    }
    
    @Test
    fun `should update user profile`() {
        // Given
        val user = userService.createUser(
            email = "profile@example.com",
            password = "password",
            role = UserRole.STANDARD_USER
        )
        val profile = userService.getUserProfile(user.id)!!
        
        // When
        val updated = profile.copy(
            displayName = "Updated Name",
            preferences = profile.preferences.copy(theme = "dark", language = "es")
        )
        userService.updateUserProfile(updated)
        
        // Then
        val retrieved = userService.getUserProfile(user.id)!!
        assertEquals("Updated Name", retrieved.displayName)
        assertEquals("dark", retrieved.preferences.theme)
        assertEquals("es", retrieved.preferences.language)
    }
}
