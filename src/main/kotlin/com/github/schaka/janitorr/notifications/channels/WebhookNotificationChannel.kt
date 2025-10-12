package com.github.schaka.janitorr.notifications.channels

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.notifications.NotificationEvent
import com.github.schaka.janitorr.notifications.config.WebhookProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Generic webhook notification channel
 */
class WebhookNotificationChannel(
    private val properties: WebhookProperties
) : NotificationChannelHandler {
    
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val httpClient: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        private val objectMapper = ObjectMapper()
    }
    
    suspend fun sendAsync(event: NotificationEvent): Boolean {
        if (properties.url.isBlank()) {
            log.warn("Webhook URL not configured")
            return false
        }
        
        var lastException: Exception? = null
        repeat(properties.retryCount) { attempt ->
            try {
                val payload = mapOf(
                    "event_type" to event.type.name,
                    "title" to event.title,
                    "message" to event.message,
                    "details" to event.details,
                    "timestamp" to event.timestamp.toString()
                )
                
                val jsonPayload = objectMapper.writeValueAsString(payload)
                
                val requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(properties.url))
                    .header("Content-Type", "application/json")
                
                properties.headers.forEach { (key, value) ->
                    requestBuilder.header(key, value)
                }
                
                val request = when (properties.method.uppercase()) {
                    "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    "PUT" -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    else -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                }.build()
                
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                
                if (response.statusCode() in 200..299) {
                    log.debug("Webhook notification sent successfully")
                    return true
                } else {
                    log.warn("Webhook returned non-success status: ${response.statusCode()} on attempt ${attempt + 1}")
                }
            } catch (e: Exception) {
                lastException = e
                log.warn("Error sending webhook notification on attempt ${attempt + 1}", e)
                if (attempt < properties.retryCount - 1) {
                    delay(1000L * (attempt + 1)) // Exponential backoff using suspend delay
                }
            }
        }
        
        log.error("Failed to send webhook notification after ${properties.retryCount} attempts", lastException)
        return false
    }
    
    override fun send(event: NotificationEvent): Boolean {
        return runBlocking { sendAsync(event) }
    }
    
    override fun test(): Boolean {
        val testEvent = NotificationEvent(
            type = com.github.schaka.janitorr.notifications.NotificationEventType.CLEANUP_COMPLETED,
            title = "Test Notification",
            message = "This is a test notification from Janitorr"
        )
        return send(testEvent)
    }
    
    override fun getType(): String = "Webhook"
}
