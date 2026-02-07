package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.config.TagDeleteSchedule
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.bazarr.BazarrRestService
import com.github.schaka.janitorr.servarr.radarr.Radarr
import com.github.schaka.janitorr.servarr.radarr.RadarrRestService
import com.github.schaka.janitorr.servarr.sonarr.Sonarr
import com.github.schaka.janitorr.servarr.sonarr.SonarrRestService
import com.github.schaka.janitorr.stats.StatsService
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("!leyden")
@Service
class TagBasedCleanupSchedule(
    mediaServerService: AbstractMediaServerService,
    jellyseerrService: JellyseerrService,
    jellystatService: StatsService,
    fileSystemProperties: FileSystemProperties,
    applicationProperties: ApplicationProperties,
    @Sonarr sonarrService: ServarrService,
    @Radarr radarrService: ServarrService,
) : AbstractCleanupSchedule(CleanupType.TAG, mediaServerService, jellyseerrService, jellystatService, fileSystemProperties, applicationProperties, sonarrService, radarrService), Schedule {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @CacheEvict(cacheNames = [SonarrRestService.CACHE_NAME, RadarrRestService.CACHE_NAME, BazarrRestService.CACHE_NAME_TV, BazarrRestService.CACHE_NAME_MOVIES])
    override fun runSchedule() {

        if (!applicationProperties.tagBasedDeletion.enabled) {
            log.info("Tag based cleanup disabled, do nothing")
            return
        }

        // initial cleanup of entire tag based directory
        if (fileSystemProperties.fromScratch) {
            mediaServerService.cleanupPath(fileSystemProperties.leavingSoonDir, TV_SHOWS, CleanupType.TAG)
            mediaServerService.cleanupPath(fileSystemProperties.leavingSoonDir, MOVIES, CleanupType.TAG)
        }

        for (tag in applicationProperties.tagBasedDeletion.schedules) {
            log.debug("Deleting TV shows and movies with tag: {}", tag)
            // Forcefully only adding links - since we're treating all tags as one equal type of cleanup
            scheduleDelete(TV_SHOWS, tag.expiration, tag.expiration, entryFilter = { item -> tagMatches(item, tag) }, true)
            scheduleDelete(MOVIES, tag.expiration, tag.expiration, entryFilter = { item -> tagMatches(item, tag) }, true)
        }

    }

    private fun tagMatches(item: LibraryItem, tag: TagDeleteSchedule): Boolean {
        return item.tags.contains(tag.tag)
    }

    override fun needToDelete(type: LibraryType): Boolean {
        return if (fileSystemProperties.access) getFreeSpacePercentage() <= applicationProperties.tagBasedDeletion.minimumFreeDiskPercent else true
    }


}