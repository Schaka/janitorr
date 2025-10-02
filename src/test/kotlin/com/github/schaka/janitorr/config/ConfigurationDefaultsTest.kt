package com.github.schaka.janitorr.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Test to ensure that cleanup tasks are disabled by default
 * to prevent automatic execution until explicitly configured by the user.
 */
internal class ConfigurationDefaultsTest {

    @Test
    fun testMediaDeletionDisabledByDefault() {
        val mediaDeletion = MediaDeletion()
        assertFalse(mediaDeletion.enabled, "MediaDeletion should be disabled by default")
    }

    @Test
    fun testTagDeletionDisabledByDefault() {
        val tagDeletion = TagDeletion()
        assertFalse(tagDeletion.enabled, "TagDeletion should be disabled by default")
    }

    @Test
    fun testEpisodeDeletionDisabledByDefault() {
        val episodeDeletion = EpisodeDeletion()
        assertFalse(episodeDeletion.enabled, "EpisodeDeletion should be disabled by default")
    }

    @Test
    fun testMediaDeletionCanBeEnabled() {
        val mediaDeletion = MediaDeletion(enabled = true)
        assertEquals(true, mediaDeletion.enabled, "MediaDeletion should be enabled when explicitly set")
    }

    @Test
    fun testTagDeletionCanBeEnabled() {
        val tagDeletion = TagDeletion(enabled = true)
        assertEquals(true, tagDeletion.enabled, "TagDeletion should be enabled when explicitly set")
    }

    @Test
    fun testEpisodeDeletionCanBeEnabled() {
        val episodeDeletion = EpisodeDeletion(enabled = true)
        assertEquals(true, episodeDeletion.enabled, "EpisodeDeletion should be enabled when explicitly set")
    }
}
