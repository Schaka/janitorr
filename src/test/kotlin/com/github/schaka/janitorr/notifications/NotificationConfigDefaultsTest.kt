package com.github.schaka.janitorr.notifications

import com.github.schaka.janitorr.notifications.config.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Test to ensure notification configuration defaults are safe
 */
internal class NotificationConfigDefaultsTest {

    @Test
    fun testNotificationsDisabledByDefault() {
        val properties = NotificationProperties()
        assertFalse(properties.enabled, "Notifications should be disabled by default")
    }

    @Test
    fun testDiscordDisabledByDefault() {
        val properties = DiscordProperties()
        assertFalse(properties.enabled, "Discord notifications should be disabled by default")
    }

    @Test
    fun testTelegramDisabledByDefault() {
        val properties = TelegramProperties()
        assertFalse(properties.enabled, "Telegram notifications should be disabled by default")
    }

    @Test
    fun testEmailDisabledByDefault() {
        val properties = EmailProperties()
        assertFalse(properties.enabled, "Email notifications should be disabled by default")
    }

    @Test
    fun testWebhookDisabledByDefault() {
        val properties = WebhookProperties()
        assertFalse(properties.enabled, "Webhook notifications should be disabled by default")
    }

    @Test
    fun testWebPushDisabledByDefault() {
        val properties = WebPushProperties()
        assertFalse(properties.enabled, "Web Push notifications should be disabled by default")
    }

    @Test
    fun testDiscordDefaultUsername() {
        val properties = DiscordProperties()
        assertEquals("Janitorr", properties.username, "Discord default username should be 'Janitorr'")
    }

    @Test
    fun testEmailDefaultPort() {
        val properties = EmailProperties()
        assertEquals(587, properties.port, "Email default port should be 587 (SMTP with TLS)")
    }

    @Test
    fun testWebhookDefaultRetryCount() {
        val properties = WebhookProperties()
        assertEquals(3, properties.retryCount, "Webhook default retry count should be 3")
    }
}
