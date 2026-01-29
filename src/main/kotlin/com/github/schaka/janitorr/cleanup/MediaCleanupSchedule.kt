package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.radarr.Radarr
import com.github.schaka.janitorr.servarr.sonarr.Sonarr
import com.github.schaka.janitorr.stats.StatsService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Duration

@Profile("!leyden")
@Service
class MediaCleanupSchedule(
    mediaServerService: AbstractMediaServerService,
    jellyseerrService: JellyseerrService,
    jellystatService: StatsService,
    fileSystemProperties: FileSystemProperties,
    applicationProperties: ApplicationProperties,
    @Sonarr sonarrService: ServarrService,
    @Radarr radarrService: ServarrService,
) : AbstractCleanupSchedule(CleanupType.MEDIA, mediaServerService, jellyseerrService, jellystatService, fileSystemProperties, applicationProperties, sonarrService, radarrService), Schedule {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }


    override fun runSchedule() {

        if (!applicationProperties.mediaDeletion.enabled) {
            log.info("Media based cleanup disabled, do nothing")
            return
        }

        val seasonExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.seasonExpiration)
        log.debug("Cleaning up TV shows older than ${seasonExpiration?.toDays()}")
        val movieExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.movieExpiration)
        log.debug("Cleaning up movies older than ${movieExpiration?.toDays()}")

        scheduleDelete(TV_SHOWS, seasonExpiration)
        scheduleDelete(MOVIES, movieExpiration)

    }

    override fun needToDelete(type: LibraryType): Boolean {

        val deleteConditions: Map<Int, Duration> = when (type) {
            TV_SHOWS -> applicationProperties.mediaDeletion.seasonExpiration
            MOVIES -> applicationProperties.mediaDeletion.movieExpiration
        }

        return determineDeletionDuration(deleteConditions) != null
    }

    override fun determineLeavingSoonDuration(type: LibraryType): Duration? {
        val deleteConditions: Map<Int, Duration> = when (type) {
            TV_SHOWS -> applicationProperties.mediaDeletion.seasonExpiration
            MOVIES -> applicationProperties.mediaDeletion.movieExpiration
        }

        val offset = applicationProperties.leavingSoonThresholdOffsetPercent
        val duration = determineLeavingSoonDuration(deleteConditions, offset)
        if (duration != null) {
            log.info(
                "Updating Leaving Soon for {} older than {} days based on threshold offset ({}%)",
                type.collectionType,
                duration.toDays(),
                offset
            )
        } else if (fileSystemProperties.access && offset > 0) {
            val freeSpacePercentage = getFreeSpacePercentage()
            val thresholds = deleteConditions.keys.sorted()
            val thresholdSummary = when {
                thresholds.isEmpty() -> "none"
                thresholds.size <= 6 -> thresholds.joinToString(",")
                else -> "${thresholds.first()}..${thresholds.last()}"
            }
            log.info(
                "Leaving Soon threshold not matched for {}: free space {}%, offset {}%, thresholds {}",
                type.collectionType,
                String.format("%.2f", freeSpacePercentage),
                offset,
                thresholdSummary
            )
        }
        return duration
    }

    private fun determineDeletionDuration(deletionConditions: Map<Int, Duration>): Duration? {

        // If we don't have access to the same file system as the library, we can't determine the actual space left and will just choose the longest expiration time available
        if (!fileSystemProperties.access) {
            return deletionConditions.entries.maxByOrNull { it.value.toDays() }?.value
        }

        val freeSpacePercentage = getFreeSpacePercentage()

        val entry = deletionConditions.entries.filter { freeSpacePercentage < it.key }.minByOrNull { it.key }
        return entry?.value
    }

    private fun determineLeavingSoonDuration(deletionConditions: Map<Int, Duration>, thresholdOffsetPercent: Int): Duration? {
        if (!fileSystemProperties.access || thresholdOffsetPercent <= 0) {
            return null
        }

        val freeSpacePercentage = getFreeSpacePercentage()
        val deleteEntry = deletionConditions.entries
            .filter { freeSpacePercentage < it.key }
            .minByOrNull { it.key }
            ?: return null

        val targetThreshold = (deleteEntry.key - thresholdOffsetPercent).coerceAtLeast(0)
        val leavingSoonEntry = deletionConditions.entries
            .filter { it.key <= targetThreshold }
            .maxByOrNull { it.key }

        if (leavingSoonEntry != null) {
            log.info(
                "Leaving Soon threshold selected: delete threshold {}%, offset {}% -> target {}% (free space {}%)",
                deleteEntry.key,
                thresholdOffsetPercent,
                targetThreshold,
                String.format("%.2f", freeSpacePercentage)
            )
        }

        return leavingSoonEntry?.value
    }
}
