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
import java.time.temporal.ChronoUnit.FOREVER

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

        val freeSpacePercentage = getFreeSpacePercentage()
        val seasonExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.seasonExpiration, freeSpacePercentage)
        log.debug("Cleaning up TV shows older than ${seasonExpiration.toDays()}")
        val movieExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.movieExpiration, freeSpacePercentage)
        log.debug("Cleaning up movies older than ${movieExpiration.toDays()}")

        val leavingSoonFreeSpacePercentage = freeSpacePercentage - applicationProperties.leavingSoonThresholdOffsetPercent
        val seasonLeavingSoonExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.seasonExpiration, leavingSoonFreeSpacePercentage)
        log.debug("Creating Leaving Soon for TV shows older than ${seasonLeavingSoonExpiration.toDays()}")
        val movieLeavingSoooExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.movieExpiration, leavingSoonFreeSpacePercentage)
        log.debug("Creating Leaving Soon for movies older than ${movieLeavingSoooExpiration.toDays()}")

        scheduleDelete(TV_SHOWS, seasonExpiration, seasonLeavingSoonExpiration)
        scheduleDelete(MOVIES, movieExpiration, movieLeavingSoooExpiration)

    }

    override fun needToDelete(type: LibraryType): Boolean {

        val deleteConditions: Map<Int, Duration> = when (type) {
            TV_SHOWS -> applicationProperties.mediaDeletion.seasonExpiration
            MOVIES -> applicationProperties.mediaDeletion.movieExpiration
        }

        val freeSpacePercentage = getFreeSpacePercentage()
        return determineDeletionDuration(deleteConditions, freeSpacePercentage) != FOREVER.duration
    }

    protected fun determineDeletionDuration(deletionConditions: Map<Int, Duration>, freeSpacePercentage: Double): Duration {

        // If we don't have access to the same file system as the library, we can't determine the actual space left and will just choose the longest expiration time available
        if (!fileSystemProperties.access) {
            log.debug("File system access is disabled - choosing longest expiration duration")
            return deletionConditions.entries.maxByOrNull { it.value.toDays() }?.value ?: FOREVER.duration
        }

        val entry = deletionConditions.entries.filter { freeSpacePercentage < it.key }.minByOrNull { it.key }
        return entry?.value ?: FOREVER.duration
    }

}
