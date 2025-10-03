package com.github.schaka.janitorr.notifications

/**
 * Main service interface for sending notifications through various channels
 */
interface NotificationService {
    
    /**
     * Send notification when a cleanup operation completes
     */
    fun sendCleanupComplete(stats: CleanupStats)
    
    /**
     * Send notification when an error occurs
     */
    fun sendError(error: String, context: String)
    
    /**
     * Send a daily summary report
     */
    fun sendDailyReport(stats: Map<String, Any>)
    
    /**
     * Send a generic notification event
     */
    fun sendNotification(event: NotificationEvent)
    
    /**
     * Test notification to verify channel configuration
     */
    fun testNotification(channel: NotificationChannel): Boolean
}

/**
 * Notification channels supported by the system
 */
enum class NotificationChannel {
    DISCORD,
    TELEGRAM,
    EMAIL,
    WEBHOOK,
    WEB_PUSH
}
