package com.github.schaka.janitorr.config

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SecurityConfigTest {

    @Test
    fun `should create security config when properties provided`() {
        val properties = SecurityProperties(
            enabled = true,
            username = "testuser",
            password = "testpass"
        )
        
        val config = SecurityConfig(properties)
        
        assertNotNull(config)
    }

    @Test
    fun `should create password encoder`() {
        val properties = SecurityProperties(enabled = true)
        val config = SecurityConfig(properties)
        
        val encoder = config.passwordEncoder()
        
        assertNotNull(encoder)
        assertTrue(encoder is BCryptPasswordEncoder)
    }

    @Test
    fun `should encode password correctly`() {
        val properties = SecurityProperties(enabled = true)
        val config = SecurityConfig(properties)
        
        val encoder = config.passwordEncoder()
        val encoded = encoder.encode("test")
        
        assertNotNull(encoded)
        assertFalse(encoded == "test") // Password should be hashed
        assertTrue(encoder.matches("test", encoded)) // But should match
    }

    @Test
    fun `should create user details service with correct user`() {
        val properties = SecurityProperties(
            enabled = true,
            username = "admin",
            password = "secret"
        )
        val config = SecurityConfig(properties)
        
        val userDetailsService = config.userDetailsService()
        
        assertNotNull(userDetailsService)
        val userDetails = userDetailsService.loadUserByUsername("admin")
        assertNotNull(userDetails)
        assertEquals("admin", userDetails.username)
        assertTrue(userDetails.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `SecurityProperties should have correct defaults`() {
        val properties = SecurityProperties()
        
        assertFalse(properties.enabled)
        assertEquals("admin", properties.username)
        assertEquals("admin", properties.password)
    }

    @Test
    fun `SecurityProperties should accept custom values`() {
        val properties = SecurityProperties(
            enabled = true,
            username = "customuser",
            password = "custompass"
        )
        
        assertTrue(properties.enabled)
        assertEquals("customuser", properties.username)
        assertEquals("custompass", properties.password)
    }
}
