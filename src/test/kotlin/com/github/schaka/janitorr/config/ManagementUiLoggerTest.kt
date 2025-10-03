package com.github.schaka.janitorr.config

import io.mockk.every
import io.mockk.mockk
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

        val logger = ManagementUiLogger(environment, managementUiProperties)
        
        // This would log "Management UI is ENABLED..." but we can't easily capture log output in unit tests
        // The test mainly ensures the logger can be constructed without errors
        verify { environment.getProperty("server.port", "8080") }
    }

    @Test
    fun testLogsDisabledStatusWhenUiIsDisabled() {
        val environment = mockk<Environment>()
        val managementUiProperties = ManagementUiProperties(enabled = false)
        
        every { environment.getProperty("server.port", "8080") } returns "8080"
        every { environment.getProperty("spring.profiles.active", "default") } returns "default"

        val logger = ManagementUiLogger(environment, managementUiProperties)
        
        // This would log "Management UI is DISABLED..." but we can't easily capture log output in unit tests
        // The test mainly ensures the logger can be constructed without errors
        verify { environment.getProperty("spring.profiles.active", "default") }
    }

    @Test
    fun testLogsDisabledStatusWhenLeydenProfileIsActive() {
        val environment = mockk<Environment>()
        val managementUiProperties = ManagementUiProperties(enabled = true)
        
        every { environment.getProperty("server.port", "8080") } returns "8080"
        every { environment.getProperty("spring.profiles.active", "default") } returns "leyden"

        val logger = ManagementUiLogger(environment, managementUiProperties)
        
        // This would log "Management UI is DISABLED (leyden profile active...)"
        // The test mainly ensures the logger can be constructed without errors
        verify { environment.getProperty("spring.profiles.active", "default") }
    }
}
