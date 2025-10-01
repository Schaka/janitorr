package com.github.schaka.janitorr.api

import com.github.schaka.janitorr.cleanup.MediaCleanupSchedule
import com.github.schaka.janitorr.cleanup.RunOnce
import com.github.schaka.janitorr.cleanup.TagBasedCleanupSchedule
import com.github.schaka.janitorr.cleanup.WeeklyEpisodeCleanupSchedule
import com.github.schaka.janitorr.config.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("!leyden")
@RestController
@RequestMapping("/api/management")
class ManagementController(
    private val mediaCleanupSchedule: MediaCleanupSchedule,
    private val tagBasedCleanupSchedule: TagBasedCleanupSchedule,
    private val weeklyEpisodeCleanupSchedule: WeeklyEpisodeCleanupSchedule,
    private val applicationProperties: ApplicationProperties,
    private val runOnce: RunOnce
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
}
