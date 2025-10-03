package com.github.schaka.janitorr.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test to ensure that Management UI configuration works correctly
 * and defaults to enabled for backwards compatibility.
 */
internal class ManagementUiPropertiesTest {

    @Test
    fun testManagementUiEnabledByDefault() {
        val managementUiProperties = ManagementUiProperties()
        assertTrue(managementUiProperties.enabled, "Management UI should be enabled by default for backwards compatibility")
    }

    @Test
    fun testManagementUiCanBeDisabled() {
        val managementUiProperties = ManagementUiProperties(enabled = false)
        assertEquals(false, managementUiProperties.enabled, "Management UI should be disabled when explicitly set to false")
    }

    @Test
    fun testManagementUiCanBeExplicitlyEnabled() {
        val managementUiProperties = ManagementUiProperties(enabled = true)
        assertEquals(true, managementUiProperties.enabled, "Management UI should be enabled when explicitly set to true")
    }
}
