package com.github.schaka.janitorr.notifications

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Test for NotificationNoOpService to ensure notifications are properly disabled by default
 */
internal class NotificationNoOpServiceTest {

    @Test
    fun testSendCleanupCompleteDoesNothing() {
        val service = NotificationNoOpService()
        val stats = CleanupStats(
            cleanupType = "Media",
            filesDeleted = 10,
            spaceFreeGB = 5.0,
            dryRun = true
        )
        
        // Should not throw exception
        service.sendCleanupComplete(stats)
    }

    @Test
    fun testSendErrorDoesNothing() {
        val service = NotificationNoOpService()
        
        // Should not throw exception
        service.sendError("Test error", "Test context")
    }

    @Test
    fun testSendDailyReportDoesNothing() {
        val service = NotificationNoOpService()
        
        // Should not throw exception
        service.sendDailyReport(mapOf("test" to "data"))
    }

    @Test
    fun testSendNotificationDoesNothing() {
        val service = NotificationNoOpService()
        val event = NotificationEvent(
            type = NotificationEventType.CLEANUP_COMPLETED,
            title = "Test",
            message = "Test message"
        )
        
        // Should not throw exception
        service.sendNotification(event)
    }

    @Test
    fun testTestNotificationReturnsFalse() {
        val service = NotificationNoOpService()
        
        val result = service.testNotification(NotificationChannel.DISCORD)
        
        assertFalse(result, "NoOp service should always return false for test notifications")
    }
}
