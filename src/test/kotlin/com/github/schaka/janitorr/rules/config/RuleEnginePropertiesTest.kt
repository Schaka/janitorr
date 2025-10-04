package com.github.schaka.janitorr.rules.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests for RuleEngineProperties
 */
internal class RuleEnginePropertiesTest {

    @Test
    fun testDefaultValues() {
        val properties = RuleEngineProperties()
        
        assertFalse(properties.enabled, "Rule engine should be disabled by default")
        assertEquals("/config/rules", properties.rulesDirectory, "Default rules directory should be /config/rules")
        assertEquals(100, properties.maxRulesPerExecution, "Default max rules should be 100")
        assertFalse(properties.enableScheduledRules, "Scheduled rules should be disabled by default")
    }

    @Test
    fun testCustomValues() {
        val properties = RuleEngineProperties(
            enabled = true,
            rulesDirectory = "/custom/rules",
            maxRulesPerExecution = 50,
            enableScheduledRules = true
        )
        
        assertEquals(true, properties.enabled)
        assertEquals("/custom/rules", properties.rulesDirectory)
        assertEquals(50, properties.maxRulesPerExecution)
        assertEquals(true, properties.enableScheduledRules)
    }
}
