package com.github.schaka.janitorr.notifications.channels

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.notifications.NotificationEvent
import com.github.schaka.janitorr.notifications.config.DiscordProperties
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Discord webhook notification channel
 */
class DiscordNotificationChannel(
    private val properties: DiscordProperties
) : NotificationChannelHandler {
    
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val httpClient: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        private val objectMapper = ObjectMapper()
    }
    
    override fun send(event: NotificationEvent): Boolean {
        if (properties.webhookUrl.isBlank()) {
            log.warn("Discord webhook URL not configured")
            return false
        }
        
        return try {
            val embed = mapOf(
                "title" to event.title,
                "description" to event.message,
                "color" to getColorForEventType(event.type.name),
                "timestamp" to event.timestamp.toString(),
                "fields" to event.details.map { (key, value) ->
                    mapOf(
                        "name" to key,
                        "value" to value.toString(),
                        "inline" to true
                    )
                }
            )
            
            val payload = mapOf(
                "username" to properties.username,
                "avatar_url" to properties.avatarUrl,
                "embeds" to listOf(embed)
            )
            
            val jsonPayload = objectMapper.writeValueAsString(payload)
            
            val request = HttpRequest.newBuilder()
                .uri(URI.create(properties.webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() in 200..299) {
                log.debug("Discord notification sent successfully")
                true
            } else {
                log.error("Failed to send Discord notification: ${response.statusCode()} - ${response.body()}")
                false
            }
        } catch (e: Exception) {
            log.error("Error sending Discord notification", e)
            false
        }
    }
    
    override fun test(): Boolean {
        val testEvent = NotificationEvent(
            type = com.github.schaka.janitorr.notifications.NotificationEventType.CLEANUP_COMPLETED,
            title = "Test Notification",
            message = "This is a test notification from Janitorr"
        )
        return send(testEvent)
    }
    
    override fun getType(): String = "Discord"
    
    private fun getColorForEventType(eventType: String): Int {
        return when (eventType) {
            "CLEANUP_COMPLETED" -> 0x00FF00 // Green
            "CLEANUP_ERROR" -> 0xFF0000 // Red
            "DISK_SPACE_WARNING" -> 0xFFA500 // Orange
            else -> 0x0099FF // Blue
        }
    }
}
