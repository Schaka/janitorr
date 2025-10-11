package com.github.schaka.janitorr.config

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment

/**
 * Test to ensure that Management UI logger correctly reports status
 */
internal class ManagementUiLoggerTest {

    @Test
    fun testLogsEnabledStatusWhenUiIsEnabled() {
        val environment = mockk<Environment>()
        val managementUiProperties = ManagementUiProperties(enabled = true)
        
        every { environment.getProperty("server.port", "8080") } returns "8080"
        every { environment.getProperty("spring.profiles.active", "default") } returns "default"

        val logger = spyk(ManagementUiLogger(environment, managementUiProperties))
        logger.logManagementUiStatus()
        
        // Verify that the environment methods were called
        verify { environment.getProperty("server.port", "8080") }
        verify { environment.getProperty("spring.profiles.active", "default") }
    }

    @Test
    fun testLogsDisabledStatusWhenUiIsDisabled() {
        val environment = mockk<Environment>()
        val managementUiProperties = ManagementUiProperties(enabled = false)
        
        every { environment.getProperty("server.port", "8080") } returns "8080"
        every { environment.getProperty("spring.profiles.active", "default") } returns "default"

        val logger = spyk(ManagementUiLogger(environment, managementUiProperties))
        logger.logManagementUiStatus()
        
        // Verify that the environment methods were called
        verify { environment.getProperty("spring.profiles.active", "default") }
    }

    @Test
    fun testLogsDisabledStatusWhenLeydenProfileIsActive() {
        val environment = mockk<Environment>()
        val managementUiProperties = ManagementUiProperties(enabled = true)
        
        every { environment.getProperty("server.port", "8080") } returns "8080"
        every { environment.getProperty("spring.profiles.active", "default") } returns "leyden"

        val logger = spyk(ManagementUiLogger(environment, managementUiProperties))
        logger.logManagementUiStatus()
        
        // Verify that the environment methods were called
        verify { environment.getProperty("spring.profiles.active", "default") }
    }
}
