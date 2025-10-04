package com.github.schaka.janitorr.notifications.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notifications")
data class NotificationProperties(
    val enabled: Boolean = false,
    val discord: DiscordProperties = DiscordProperties(),
    val telegram: TelegramProperties = TelegramProperties(),
    val email: EmailProperties = EmailProperties(),
    val webhook: WebhookProperties = WebhookProperties(),
    val webPush: WebPushProperties = WebPushProperties()
)

data class DiscordProperties(
    val enabled: Boolean = false,
    val webhookUrl: String = "",
    val username: String = "Janitorr",
    val avatarUrl: String = ""
)

data class TelegramProperties(
    val enabled: Boolean = false,
    val botToken: String = "",
    val chatId: String = ""
)

data class EmailProperties(
    val enabled: Boolean = false,
    val host: String = "smtp.gmail.com",
    val port: Int = 587,
    val username: String = "",
    val password: String = "",
    val from: String = "",
    val to: List<String> = emptyList(),
    val useTls: Boolean = true
)

data class WebhookProperties(
    val enabled: Boolean = false,
    val url: String = "",
    val method: String = "POST",
    val headers: Map<String, String> = emptyMap(),
    val retryCount: Int = 3
)

data class WebPushProperties(
    val enabled: Boolean = false,
    val publicKey: String = "",
    val privateKey: String = ""
)
