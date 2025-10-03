package com.github.schaka.janitorr.notifications.channels

import com.github.schaka.janitorr.notifications.NotificationEvent
import com.github.schaka.janitorr.notifications.config.EmailProperties
import org.slf4j.LoggerFactory
import java.util.Properties
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

/**
 * Email (SMTP) notification channel
 */
class EmailNotificationChannel(
    private val properties: EmailProperties
) : NotificationChannelHandler {
    
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }
    
    override fun send(event: NotificationEvent): Boolean {
        if (!isConfigured()) {
            log.warn("Email configuration incomplete")
            return false
        }
        
        return try {
            val session = createSession()
            val message = MimeMessage(session)
            
            message.setFrom(InternetAddress(properties.from))
            properties.to.forEach { recipient ->
                message.addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
            }
            
            message.subject = "[Janitorr] ${event.title}"
            message.setContent(buildHtmlContent(event), "text/html; charset=utf-8")
            
            Transport.send(message)
            log.debug("Email notification sent successfully to ${properties.to.size} recipient(s)")
            true
        } catch (e: Exception) {
            log.error("Error sending email notification", e)
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
    
    override fun getType(): String = "Email"
    
    private fun isConfigured(): Boolean {
        return properties.host.isNotBlank() &&
               properties.username.isNotBlank() &&
               properties.password.isNotBlank() &&
               properties.from.isNotBlank() &&
               properties.to.isNotEmpty()
    }
    
    private fun createSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", properties.host)
            put("mail.smtp.port", properties.port.toString())
            
            if (properties.useTls) {
                put("mail.smtp.starttls.enable", "true")
            }
        }
        
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(properties.username, properties.password)
            }
        })
    }
    
    private fun buildHtmlContent(event: NotificationEvent): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .details { background-color: #f4f4f4; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #777; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Janitorr Notification</h1>
                </div>
                <div class="content">
                    <h2>${event.title}</h2>
                    <p>${event.message}</p>
                    ${if (event.details.isNotEmpty()) buildDetailsSection(event.details) else ""}
                </div>
                <div class="footer">
                    <p>Sent by Janitorr at ${event.timestamp}</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    private fun buildDetailsSection(details: Map<String, Any>): String {
        val items = details.entries.joinToString("") { (key, value) ->
            "<p><strong>$key:</strong> $value</p>"
        }
        return """
            <div class="details">
                <h3>Details</h3>
                $items
            </div>
        """.trimIndent()
    }
}
