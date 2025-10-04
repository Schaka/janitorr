package com.github.schaka.janitorr.notifications.channels

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.janitorr.notifications.NotificationEvent
import com.github.schaka.janitorr.notifications.config.TelegramProperties
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Telegram bot notification channel
 */
class TelegramNotificationChannel(
    private val properties: TelegramProperties
) : NotificationChannelHandler {
    
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val httpClient: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        private val objectMapper = ObjectMapper()
    }
    
    override fun send(event: NotificationEvent): Boolean {
        if (properties.botToken.isBlank() || properties.chatId.isBlank()) {
            log.warn("Telegram bot token or chat ID not configured")
            return false
        }
        
        return try {
            val message = buildMessage(event)
            val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8)
            
            val url = "https://api.telegram.org/bot${properties.botToken}/sendMessage?chat_id=${properties.chatId}&text=${encodedMessage}&parse_mode=HTML"
            
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() in 200..299) {
                log.debug("Telegram notification sent successfully")
                true
            } else {
                log.error("Failed to send Telegram notification: ${response.statusCode()} - ${response.body()}")
                false
            }
        } catch (e: Exception) {
            log.error("Error sending Telegram notification", e)
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
    
    override fun getType(): String = "Telegram"
    
    private fun buildMessage(event: NotificationEvent): String {
        val sb = StringBuilder()
        sb.append("<b>${event.title}</b>\n\n")
        sb.append("${event.message}\n\n")
        
        if (event.details.isNotEmpty()) {
            sb.append("<b>Details:</b>\n")
            event.details.forEach { (key, value) ->
                sb.append("• <i>$key</i>: $value\n")
            }
        }
        
        sb.append("\n<i>${event.timestamp}</i>")
        return sb.toString()
    }
}
