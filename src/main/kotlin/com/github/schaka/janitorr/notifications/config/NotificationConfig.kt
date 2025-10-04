package com.github.schaka.janitorr.notifications.config

import com.github.schaka.janitorr.notifications.NotificationNoOpService
import com.github.schaka.janitorr.notifications.NotificationService
import com.github.schaka.janitorr.notifications.NotificationServiceImpl
import com.github.schaka.janitorr.notifications.channels.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Configuration for the notification system.
 * Follows the same pattern as other services in Janitorr.
 * 
 * IMPORTANT: Excluded from "leyden" profile for AOT compatibility.
 */
@Profile("!leyden")
@Configuration
@EnableConfigurationProperties(NotificationProperties::class)
class NotificationConfig {
    
    @Bean
    @ConditionalOnProperty(prefix = "notifications", name = ["enabled"], havingValue = "true")
    fun notificationService(
        properties: NotificationProperties,
        channels: List<NotificationChannelHandler>
    ): NotificationService {
        return NotificationServiceImpl(properties, channels)
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "notifications", name = ["enabled"], havingValue = "false", matchIfMissing = true)
    fun notificationNoOpService(): NotificationService {
        return NotificationNoOpService()
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "notifications.discord", name = ["enabled"], havingValue = "true")
    fun discordChannel(properties: NotificationProperties): NotificationChannelHandler {
        return DiscordNotificationChannel(properties.discord)
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "notifications.telegram", name = ["enabled"], havingValue = "true")
    fun telegramChannel(properties: NotificationProperties): NotificationChannelHandler {
        return TelegramNotificationChannel(properties.telegram)
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "notifications.email", name = ["enabled"], havingValue = "true")
    fun emailChannel(properties: NotificationProperties): NotificationChannelHandler {
        return EmailNotificationChannel(properties.email)
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "notifications.webhook", name = ["enabled"], havingValue = "true")
    fun webhookChannel(properties: NotificationProperties): NotificationChannelHandler {
        return WebhookNotificationChannel(properties.webhook)
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "notifications.web-push", name = ["enabled"], havingValue = "true")
    fun webPushChannel(properties: NotificationProperties): NotificationChannelHandler {
        return WebPushNotificationChannel(properties.webPush)
    }
}
