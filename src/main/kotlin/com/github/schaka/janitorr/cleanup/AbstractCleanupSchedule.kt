package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.stats.StatsService
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.LocalDateTime

abstract class AbstractCleanupSchedule(
    protected val cleanupType: CleanupType,
    protected val mediaServerService: AbstractMediaServerService,
    protected val jellyseerrService: JellyseerrService,
    protected val statsService: StatsService,
    protected val fileSystemProperties: FileSystemProperties,
    protected val applicationProperties: ApplicationProperties,
    protected val sonarrService: ServarrService,
    protected val radarrService: ServarrService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    protected fun scheduleDelete(libraryType: LibraryType, expiration: Duration?, entryFilter: (LibraryItem) -> Boolean = { true }, onlyAddLinks: Boolean = false) {

        if (!needToDelete(libraryType)) {
            log.info("Not deleting ${libraryType.collectionType} because minimum disk threshold was not reached.")
            if (fileSystemProperties.access) {
                log.info("Free disk space: ${getFreeSpacePercentage()}%")
            }
            return
        }

        if (expiration == null) {
            log.error("Incorrectly determined expiration duration")
            return
        }

        when (libraryType) {
            TV_SHOWS -> cleanupMediaType(libraryType, sonarrService, expiration, this::deleteTvShows, entryFilter, onlyAddLinks)
            MOVIES -> cleanupMediaType(libraryType, radarrService, expiration, this::deleteMovies, entryFilter, onlyAddLinks)
        }

    }

    abstract fun needToDelete(type: LibraryType): Boolean

    /**
     * Convert to full days and do some math.
     * This should probably work just letting the user set the duration entirely. But I think forcing full days will avoid some user errors.
     */
    private fun cleanupMediaType(libraryType: LibraryType, servarrService: ServarrService, expiration: Duration,
                                 deleteTask: (List<LibraryItem>) -> Unit,
                                 entryFilter: (LibraryItem) -> Boolean,
                                 onlyAddLinks: Boolean = false
                                 ) {

        val today = LocalDateTime.now()
        val leavingSoonExpiration = applicationProperties.leavingSoon.toDays()
        val expirationDays = expiration.toDays()

        val servarrEntries = servarrService.getEntries().filter(entryFilter)
        // prefilter, so expensive operations like mediaServerId and watchHistory population don't have to run on the entire library
        // this includes all entries that are already past their deletion window and the upcoming ones necessary for Leaving Soon
        var deletionCandidates = servarrEntries.filter { it.importedDate.plusDays(expirationDays - leavingSoonExpiration) < today }

        mediaServerService.populateMediaServerIds(deletionCandidates, libraryType,!applicationProperties.wholeTvShow)
        statsService.populateWatchHistory(deletionCandidates, libraryType)

        // Filter out favorited items
        deletionCandidates = mediaServerService.filterOutFavorites(deletionCandidates, libraryType)

        val leavingSoon = deletionCandidates.filter { it.historyAge.plusDays(expirationDays) >= today }
        mediaServerService.updateLeavingSoon(cleanupType, libraryType, leavingSoon, onlyAddLinks)

        val toDeleteMedia = deletionCandidates.filter { it.historyAge.plusDays(expirationDays) < today }
        deleteTask(toDeleteMedia)

        if (log.isTraceEnabled) {
            servarrEntries.filter { it.historyAge.plusDays(expirationDays) >= today }.forEach( ::logKeep)
        }
    }

    protected fun getFreeSpacePercentage(): Double {
        val filesystem = File(fileSystemProperties.freeSpaceCheckDir)
        return (filesystem.usableSpace.toDouble() / filesystem.totalSpace.toDouble()) * 100
    }

    protected fun deleteMovies(toDeleteMovies: List<LibraryItem>) {
        radarrService.removeEntries(toDeleteMovies)

        val cannotDeleteMovies = toDeleteMovies.filter { it.seeding }
        val deletedMovies = toDeleteMovies.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedMovies)
        mediaServerService.cleanupMovies(deletedMovies)
        mediaServerService.updateLeavingSoon(cleanupType, MOVIES, cannotDeleteMovies, true)
    }

    protected fun deleteTvShows(toDeleteShows: List<LibraryItem>) {
        sonarrService.removeEntries(toDeleteShows)

        val cannotDeleteShow = toDeleteShows.filter { it.seeding }
        val deletedShows = toDeleteShows.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedShows)
        mediaServerService.cleanupTvShows(deletedShows)
        mediaServerService.updateLeavingSoon(cleanupType, TV_SHOWS, cannotDeleteShow, true)
    }

    private fun logKeep(item: LibraryItem) {
        val today = LocalDateTime.now()
        log.trace("{} - IMDB ({}) not selected for deletion - downloaded {} - watched {} - age: {}",
            item.libraryPath,
            item.imdbId,
            item.importedDate,
            item.lastSeen ?: LocalDateTime.MIN,
            Duration.between(today, item.historyAge))
    }

}