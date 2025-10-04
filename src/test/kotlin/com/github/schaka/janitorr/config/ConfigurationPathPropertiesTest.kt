package com.github.schaka.janitorr.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test to ensure that configuration path properties work correctly
 * with proper defaults for backwards compatibility.
 */
internal class ConfigurationPathPropertiesTest {

    @Test
    fun testConfigurationPathPropertiesDefaults() {
        val properties = ConfigurationPathProperties()
        
        assertEquals(
            "/config/application.yml", 
            properties.configFile, 
            "Config file path should default to /config/application.yml"
        )
        assertEquals(
            "/config/backups", 
            properties.backupDirectory, 
            "Backup directory should default to /config/backups"
        )
    }

    @Test
    fun testConfigurationPathPropertiesCustomValues() {
        val properties = ConfigurationPathProperties(
            configFile = "/custom/path/config.yml",
            backupDirectory = "/custom/backups"
        )
        
        assertEquals(
            "/custom/path/config.yml", 
            properties.configFile, 
            "Config file path should be customizable"
        )
        assertEquals(
            "/custom/backups", 
            properties.backupDirectory, 
            "Backup directory should be customizable"
        )
    }
}
