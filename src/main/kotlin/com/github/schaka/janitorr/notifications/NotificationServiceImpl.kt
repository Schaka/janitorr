package com.github.schaka.janitorr.notifications

import com.github.schaka.janitorr.notifications.channels.NotificationChannelHandler
import com.github.schaka.janitorr.notifications.config.NotificationProperties
import org.slf4j.LoggerFactory

/**
 * Main implementation of the notification service that coordinates
 * sending notifications through multiple channels.
 */
class NotificationServiceImpl(
    private val properties: NotificationProperties,
    private val channels: List<NotificationChannelHandler>
) : NotificationService {
    
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }
    
    init {
        log.info("Notification service initialized with ${channels.size} channel(s): ${channels.map { it.getType() }}")
    }
    
    override fun sendCleanupComplete(stats: CleanupStats) {
        val event = NotificationEvent(
            type = NotificationEventType.CLEANUP_COMPLETED,
            title = "Cleanup Completed: ${stats.cleanupType}",
            message = buildCleanupMessage(stats),
            details = mapOf(
                "Files Deleted" to stats.filesDeleted,
                "Space Freed (GB)" to stats.spaceFreeGB,
                "Dry Run" to stats.dryRun,
                "Errors" to stats.errors.size
            )
        )
        sendNotification(event)
    }
    
    override fun sendError(error: String, context: String) {
        val event = NotificationEvent(
            type = NotificationEventType.CLEANUP_ERROR,
            title = "Error in $context",
            message = error,
            details = mapOf("Context" to context)
        )
        sendNotification(event)
    }
    
    override fun sendDailyReport(stats: Map<String, Any>) {
        val event = NotificationEvent(
            type = NotificationEventType.DAILY_REPORT,
            title = "Daily Janitorr Report",
            message = "Daily summary of Janitorr operations",
            details = stats
        )
        sendNotification(event)
    }
    
    override fun sendNotification(event: NotificationEvent) {
        if (channels.isEmpty()) {
            log.warn("No notification channels configured")
            return
        }
        
        channels.forEach { channel ->
            try {
                val success = channel.send(event)
                if (success) {
                    log.debug("Notification sent successfully via ${channel.getType()}")
                } else {
                    log.warn("Failed to send notification via ${channel.getType()}")
                }
            } catch (e: Exception) {
                log.error("Error sending notification via ${channel.getType()}", e)
            }
        }
    }
    
    override fun testNotification(channel: com.github.schaka.janitorr.notifications.NotificationChannel): Boolean {
        val channelImpl = channels.find { it.getType().equals(channel.name, ignoreCase = true) }
        if (channelImpl == null) {
            log.warn("Channel ${channel.name} not configured or not enabled")
            return false
        }
        
        return try {
            channelImpl.test()
        } catch (e: Exception) {
            log.error("Error testing channel ${channel.name}", e)
            false
        }
    }
    
    private fun buildCleanupMessage(stats: CleanupStats): String {
        return if (stats.dryRun) {
            "🔍 Dry Run: Would have deleted ${stats.filesDeleted} file(s), freeing ${String.format("%.2f", stats.spaceFreeGB)} GB"
        } else {
            "✅ Deleted ${stats.filesDeleted} file(s), freed ${String.format("%.2f", stats.spaceFreeGB)} GB"
        } + if (stats.errors.isNotEmpty()) {
            "\n⚠️ ${stats.errors.size} error(s) occurred"
        } else ""
    }
}
