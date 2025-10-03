package com.github.schaka.janitorr.notifications.channels

import com.github.schaka.janitorr.notifications.NotificationEvent

/**
 * Interface for notification channel implementations
 */
interface NotificationChannelHandler {
    /**
     * Send a notification through this channel
     */
    fun send(event: NotificationEvent): Boolean
    
    /**
     * Test if the channel is properly configured
     */
    fun test(): Boolean
    
    /**
     * Get the channel type
     */
    fun getType(): String
}
