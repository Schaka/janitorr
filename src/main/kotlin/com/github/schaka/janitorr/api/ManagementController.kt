package com.github.schaka.janitorr.api

import com.github.schaka.janitorr.cleanup.MediaCleanupSchedule
import com.github.schaka.janitorr.cleanup.RunOnce
import com.github.schaka.janitorr.cleanup.TagBasedCleanupSchedule
import com.github.schaka.janitorr.cleanup.WeeklyEpisodeCleanupSchedule
import com.github.schaka.janitorr.config.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for manual management operations and system status.
 * 
 * This controller provides endpoints for:
 * - Checking system status and configuration
 * - Manually triggering cleanup operations
 * 
 * IMPORTANT: This controller is excluded from the "leyden" profile (@Profile("!leyden")).
 * 
 * The "leyden" profile is ONLY used during Docker image builds for AOT (Ahead-Of-Time) cache generation.
 * It should NEVER be active at runtime. If users set SPRING_PROFILES_ACTIVE=leyden at runtime,
 * this controller will not load, causing 404 errors on all management API endpoints.
 * 
 * See documentation:
 * - docs/wiki/en/Docker-Compose-Setup.md - "Spring Boot Profiles Configuration"
 * - docs/wiki/es/Configuracion-Docker-Compose.md - "Configuración de Perfiles de Spring Boot"
 * - docs/wiki/en/FAQ.md - "Why does the Management UI return 404 errors?"
 */
@Profile("!leyden")
@ConditionalOnProperty(prefix = "management.ui", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@RestController
@RequestMapping("/api/management")
class ManagementController(
    private val mediaCleanupSchedule: MediaCleanupSchedule,
    private val tagBasedCleanupSchedule: TagBasedCleanupSchedule,
    private val weeklyEpisodeCleanupSchedule: WeeklyEpisodeCleanupSchedule,
    private val applicationProperties: ApplicationProperties,
    private val runOnce: RunOnce,
    private val notificationService: com.github.schaka.janitorr.notifications.NotificationService
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @PostMapping("/cleanup/media")
    fun triggerMediaCleanup(): Map<String, Any> {
        log.info("Manual trigger of media cleanup requested")
        return try {
            mediaCleanupSchedule.runSchedule()
            mapOf(
                "success" to true,
                "message" to "Media cleanup completed successfully",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            log.error("Error during manual media cleanup", e)
            mapOf(
                "success" to false,
                "message" to "Error: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            )
        }
    }

    @PostMapping("/cleanup/tag-based")
    fun triggerTagBasedCleanup(): Map<String, Any> {
        log.info("Manual trigger of tag-based cleanup requested")
        return try {
            tagBasedCleanupSchedule.runSchedule()
            mapOf(
                "success" to true,
                "message" to "Tag-based cleanup completed successfully",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            log.error("Error during manual tag-based cleanup", e)
            mapOf(
                "success" to false,
                "message" to "Error: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            )
        }
    }

    @PostMapping("/cleanup/episodes")
    fun triggerEpisodeCleanup(): Map<String, Any> {
        log.info("Manual trigger of episode cleanup requested")
        return try {
            weeklyEpisodeCleanupSchedule.runSchedule()
            mapOf(
                "success" to true,
                "message" to "Episode cleanup completed successfully",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            log.error("Error during manual episode cleanup", e)
            mapOf(
                "success" to false,
                "message" to "Error: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            )
        }
    }

    @GetMapping("/status")
    fun getStatus(): Map<String, Any> {
        return mapOf(
            "dryRun" to applicationProperties.dryRun,
            "runOnce" to applicationProperties.runOnce,
            "mediaDeletionEnabled" to applicationProperties.mediaDeletion.enabled,
            "tagBasedDeletionEnabled" to applicationProperties.tagBasedDeletion.enabled,
            "episodeDeletionEnabled" to applicationProperties.episodeDeletion.enabled,
            "hasMediaCleanupRun" to runOnce.hasMediaCleanupRun,
            "hasTagBasedCleanupRun" to runOnce.hasTagBasedCleanupRun,
            "hasWeeklyEpisodeCleanupRun" to runOnce.hasWeeklyEpisodeCleanupRun,
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    @PostMapping("/notifications/test/{channel}")
    fun testNotification(@org.springframework.web.bind.annotation.PathVariable channel: String): Map<String, Any> {
        log.info("Testing notification channel: $channel")
        return try {
            val notificationChannel = com.github.schaka.janitorr.notifications.NotificationChannel.valueOf(channel.uppercase())
            val success = notificationService.testNotification(notificationChannel)
            
            mapOf(
                "success" to success,
                "message" to if (success) {
                    "Test notification sent successfully to $channel"
                } else {
                    "Failed to send test notification to $channel. Check if the channel is enabled and configured."
                },
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: IllegalArgumentException) {
            log.error("Invalid notification channel: $channel", e)
            mapOf(
                "success" to false,
                "message" to "Invalid notification channel: $channel. Valid channels are: ${com.github.schaka.janitorr.notifications.NotificationChannel.entries.joinToString()}",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            log.error("Error testing notification channel: $channel", e)
            mapOf(
                "success" to false,
                "message" to "Error: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            )
        }
    }
    
    @PostMapping("/notifications/test/{channel}")
    fun testNotification(@org.springframework.web.bind.annotation.PathVariable channel: String): Map<String, Any> {
        log.info("Testing notification channel: $channel")
        return try {
            val notificationChannel = com.github.schaka.janitorr.notifications.NotificationChannel.valueOf(channel.uppercase())
            val success = notificationService.testNotification(notificationChannel)
            
            mapOf(
                "success" to success,
                "message" to if (success) {
                    "Test notification sent successfully to $channel"
                } else {
                    "Failed to send test notification to $channel. Check if the channel is enabled and configured."
                },
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: IllegalArgumentException) {
            log.error("Invalid notification channel: $channel", e)
            mapOf(
                "success" to false,
                "message" to "Invalid notification channel: $channel. Valid channels are: ${com.github.schaka.janitorr.notifications.NotificationChannel.entries.joinToString()}",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            log.error("Error testing notification channel: $channel", e)
            mapOf(
                "success" to false,
                "message" to "Error: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            )
        }
    }
    }
}
