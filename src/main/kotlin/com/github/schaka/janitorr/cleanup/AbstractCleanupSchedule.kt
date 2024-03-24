package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.jellystat.JellystatService
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.LocalDateTime

abstract class AbstractCleanupSchedule(
        protected val mediaServerService: MediaServerService,
        protected val jellyseerrService: JellyseerrService,
        protected val jellystatService: JellystatService,
        protected val fileSystemProperties: FileSystemProperties,
        protected val applicationProperties: ApplicationProperties,
        protected val sonarrService: ServarrService,
        protected val radarrService: ServarrService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    protected fun scheduleDelete(type: LibraryType, expiration: Duration?, entryFilter: (LibraryItem) -> Boolean = { true }) {

        if (!needToDelete(type)) {
            log.info("Not deleting ${type.collectionName} because minimum disk threshold was not reached.")
            if (fileSystemProperties.access) {
                log.info("Free disk space: ${getFreeSpacePercentage()}%")
            }
            return
        }

        if (expiration == null) {
            log.error("Incorrectly determined expiration duration")
            return
        }

        when (type) {
            TV_SHOWS -> cleanupMediaType(type, sonarrService, expiration, this::deleteTvShows, entryFilter)
            MOVIES -> cleanupMediaType(type, radarrService, expiration, this::deleteMovies, entryFilter)
        }

    }

    abstract fun needToDelete(type: LibraryType): Boolean

    /**
     * Convert to full days and do some math.
     * This should probably work just letting the user set the duration entirely. But I think forcing full days will avoid some user errors.
     */
    private fun cleanupMediaType(type: LibraryType, servarrService: ServarrService, expiration: Duration,
                                 deleteTask: (List<LibraryItem>) -> Unit,
                                 entryFilter: (LibraryItem) -> Boolean) {

        val today = LocalDateTime.now()
        val leavingSoonExpiration = applicationProperties.leavingSoon.toDays()
        val expirationDays = expiration.toDays()

        val servarrEntries = servarrService.getEntries().filter(entryFilter)
        jellystatService.populateWatchHistory(servarrEntries, type)

        val leavingSoon = servarrEntries.filter { it.historyAge.plusDays(expirationDays - leavingSoonExpiration) < today && it.historyAge.plusDays(expirationDays) >= today }
        mediaServerService.updateGoneSoon(type, leavingSoon)

        val toDeleteMedia = servarrEntries.filter { it.historyAge.plusDays(expirationDays) < today }
        deleteTask(toDeleteMedia)
    }

    protected fun getFreeSpacePercentage(): Double {
        val filesystem = File("/")
        return (filesystem.freeSpace.toDouble() / filesystem.totalSpace.toDouble()) * 100
    }

    protected fun deleteMovies(toDeleteMovies: List<LibraryItem>) {
        radarrService.removeEntries(toDeleteMovies)

        val cannotDeleteMovies = toDeleteMovies.filter { it.seeding }
        val deletedMovies = toDeleteMovies.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedMovies)
        mediaServerService.cleanupMovies(deletedMovies)
        mediaServerService.updateGoneSoon(MOVIES, cannotDeleteMovies, true)
    }

    protected fun deleteTvShows(toDeleteShows: List<LibraryItem>) {
        sonarrService.removeEntries(toDeleteShows)

        val cannotDeleteShow = toDeleteShows.filter { it.seeding }
        val deletedShows = toDeleteShows.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedShows)
        mediaServerService.cleanupTvShows(deletedShows)
        mediaServerService.updateGoneSoon(TV_SHOWS, cannotDeleteShow, true)
    }

}