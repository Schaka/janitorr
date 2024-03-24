package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.jellystat.JellystatService
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.radarr.Radarr
import com.github.schaka.janitorr.servarr.radarr.RadarrRestService
import com.github.schaka.janitorr.servarr.sonarr.Sonarr
import com.github.schaka.janitorr.servarr.sonarr.SonarrRestService
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class MediaCleanupSchedule(
        mediaServerService: MediaServerService,
        jellyseerrService: JellyseerrService,
        jellystatService: JellystatService,
        fileSystemProperties: FileSystemProperties,
        applicationProperties: ApplicationProperties,
        @Sonarr sonarrService: ServarrService,
        @Radarr radarrService: ServarrService,
) : AbstractCleanupSchedule(mediaServerService, jellyseerrService, jellystatService, fileSystemProperties, applicationProperties, sonarrService, radarrService) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    // run every hour
    @CacheEvict(cacheNames = [SonarrRestService.CACHE_NAME, RadarrRestService.CACHE_NAME])
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun runSchedule() {

        if (!applicationProperties.mediaDeletion.enabled) {
            log.info("Media based cleanup disabled, do nothing")
            return
        }

        val seasonExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.seasonExpiration)
        val movieExpiration = determineDeletionDuration(applicationProperties.mediaDeletion.movieExpiration)

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

    private fun determineDeletionDuration(deletionConditions: Map<Int, Duration>): Duration? {

        // If we don't have access to the same file system as the library, we can't determine the actual space left and will just choose the longest expiration time available
        if (!fileSystemProperties.access) {
            return deletionConditions.entries.maxByOrNull { it.value.toDays() }?.value
        }

        val freeSpacePercentage = getFreeSpacePercentage()

        val entry = deletionConditions.entries.filter { freeSpacePercentage < it.key }.minByOrNull { it.key }
        return entry?.value
    }


}