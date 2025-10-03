package com.github.schaka.janitorr.notifications

import org.slf4j.LoggerFactory

/**
 * No-operation notification service used when notifications are disabled.
 * Follows the same pattern as StatsNoOpService.
 */
class NotificationNoOpService : NotificationService {
    
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }
    
    override fun sendCleanupComplete(stats: CleanupStats) {
        log.debug("Notifications disabled - cleanup stats: $stats")
    }
    
    override fun sendError(error: String, context: String) {
        log.debug("Notifications disabled - error in $context: $error")
    }
    
    override fun sendDailyReport(stats: Map<String, Any>) {
        log.debug("Notifications disabled - daily report: $stats")
    }
    
    override fun sendNotification(event: NotificationEvent) {
        log.debug("Notifications disabled - event: ${event.type}")
    }
    
    override fun testNotification(channel: NotificationChannel): Boolean {
        log.info("Notifications disabled - cannot test channel $channel")
        return false
    }
}
