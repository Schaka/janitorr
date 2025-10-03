package com.github.schaka.janitorr.notifications

import com.github.schaka.janitorr.notifications.channels.NotificationChannelHandler
import com.github.schaka.janitorr.notifications.config.NotificationProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Test for NotificationServiceImpl
 */
internal class NotificationServiceImplTest {

    @Test
    fun testSendCleanupCompleteCallsChannels() {
        val mockChannel = mockk<NotificationChannelHandler>(relaxed = true)
        every { mockChannel.getType() } returns "TestChannel"
        every { mockChannel.send(any()) } returns true
        
        val properties = NotificationProperties(enabled = true)
        val service = NotificationServiceImpl(properties, listOf(mockChannel))
        
        val stats = CleanupStats(
            cleanupType = "Media",
            filesDeleted = 5,
            spaceFreeGB = 10.0,
            dryRun = true
        )
        
        service.sendCleanupComplete(stats)
        
        verify(exactly = 1) { mockChannel.send(any()) }
    }

    @Test
    fun testSendErrorCallsChannels() {
        val mockChannel = mockk<NotificationChannelHandler>(relaxed = true)
        every { mockChannel.getType() } returns "TestChannel"
        every { mockChannel.send(any()) } returns true
        
        val properties = NotificationProperties(enabled = true)
        val service = NotificationServiceImpl(properties, listOf(mockChannel))
        
        service.sendError("Test error", "Test context")
        
        verify(exactly = 1) { mockChannel.send(any()) }
    }

    @Test
    fun testSendDailyReportCallsChannels() {
        val mockChannel = mockk<NotificationChannelHandler>(relaxed = true)
        every { mockChannel.getType() } returns "TestChannel"
        every { mockChannel.send(any()) } returns true
        
        val properties = NotificationProperties(enabled = true)
        val service = NotificationServiceImpl(properties, listOf(mockChannel))
        
        service.sendDailyReport(mapOf("key" to "value"))
        
        verify(exactly = 1) { mockChannel.send(any()) }
    }

    @Test
    fun testTestNotificationCallsCorrectChannel() {
        val mockChannel = mockk<NotificationChannelHandler>(relaxed = true)
        every { mockChannel.getType() } returns "DISCORD"
        every { mockChannel.test() } returns true
        
        val properties = NotificationProperties(enabled = true)
        val service = NotificationServiceImpl(properties, listOf(mockChannel))
        
        val result = service.testNotification(NotificationChannel.DISCORD)
        
        assertTrue(result, "Test notification should return true when channel test succeeds")
        verify(exactly = 1) { mockChannel.test() }
    }

    @Test
    fun testMultipleChannelsAllReceiveNotifications() {
        val mockChannel1 = mockk<NotificationChannelHandler>(relaxed = true)
        val mockChannel2 = mockk<NotificationChannelHandler>(relaxed = true)
        
        every { mockChannel1.getType() } returns "Channel1"
        every { mockChannel1.send(any()) } returns true
        every { mockChannel2.getType() } returns "Channel2"
        every { mockChannel2.send(any()) } returns true
        
        val properties = NotificationProperties(enabled = true)
        val service = NotificationServiceImpl(properties, listOf(mockChannel1, mockChannel2))
        
        val event = NotificationEvent(
            type = NotificationEventType.CLEANUP_COMPLETED,
            title = "Test",
            message = "Test message"
        )
        
        service.sendNotification(event)
        
        verify(exactly = 1) { mockChannel1.send(any()) }
        verify(exactly = 1) { mockChannel2.send(any()) }
    }
}
