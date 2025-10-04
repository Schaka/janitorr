package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.notifications.CleanupStats
import com.github.schaka.janitorr.notifications.NotificationService
import com.github.schaka.janitorr.stats.StatsService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.metrics.MetricsService
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

abstract class AbstractCleanupSchedule(
    protected val cleanupType: CleanupType,
    protected val mediaServerService: AbstractMediaServerService,
    protected val jellyseerrService: JellyseerrService,
    protected val jellystatService: StatsService,
    protected val fileSystemProperties: FileSystemProperties,
    protected val applicationProperties: ApplicationProperties,
    protected val runOnce: RunOnce,
    protected val sonarrService: ServarrService,
    protected val radarrService: ServarrService,
    protected val metricsService: MetricsService,
    protected val notificationService: NotificationService,
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
        jellystatService.populateWatchHistory(servarrEntries, libraryType)

        val leavingSoon = servarrEntries.filter { it.historyAge.plusDays(expirationDays - leavingSoonExpiration) < today && it.historyAge.plusDays(expirationDays) >= today }
        mediaServerService.updateLeavingSoon(cleanupType, libraryType, leavingSoon, onlyAddLinks)

        val toDeleteMedia = servarrEntries.filter { it.historyAge.plusDays(expirationDays) < today }
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
        val initialCount = toDeleteMovies.size
        val errors = mutableListOf<String>()
        
        try {
            radarrService.removeEntries(toDeleteMovies)

            val cannotDeleteMovies = toDeleteMovies.filter { it.seeding }
            val deletedMovies = toDeleteMovies.filter { !it.seeding }

            // Calculate space freed from successfully deleted movies
            val spaceFreed = deletedMovies.sumOf { it.sizeInBytes }
            if (deletedMovies.isNotEmpty()) {
                metricsService.recordCleanup("movies", deletedMovies.size, spaceFreed)
            }

            jellyseerrService.cleanupRequests(deletedMovies)
            mediaServerService.cleanupMovies(deletedMovies)
            mediaServerService.updateLeavingSoon(cleanupType, MOVIES, cannotDeleteMovies, true)
            
            sendCleanupNotification(deletedMovies.size, errors, spaceFreed)
        } catch (e: Exception) {
            log.error("Error during movie cleanup", e)
            errors.add("Movie cleanup error: ${e.message}")
            sendCleanupNotification(0, errors, 0L)
        }
    }

    protected fun deleteTvShows(toDeleteShows: List<LibraryItem>) {
        val initialCount = toDeleteShows.size
        val errors = mutableListOf<String>()
        
        try {
            sonarrService.removeEntries(toDeleteShows)

            val cannotDeleteShow = toDeleteShows.filter { it.seeding }
            val deletedShows = toDeleteShows.filter { !it.seeding }

            // Calculate space freed from successfully deleted shows/seasons
            val spaceFreed = deletedShows.sumOf { it.sizeInBytes }
            if (deletedShows.isNotEmpty()) {
                val type = if (applicationProperties.wholeTvShow) "shows" else "episodes"
                metricsService.recordCleanup(type, deletedShows.size, spaceFreed)
            }

            jellyseerrService.cleanupRequests(deletedShows)
            mediaServerService.cleanupTvShows(deletedShows)
            mediaServerService.updateLeavingSoon(cleanupType, TV_SHOWS, cannotDeleteShow, true)
            
            sendCleanupNotification(deletedShows.size, errors, spaceFreed)
        } catch (e: Exception) {
            log.error("Error during TV show cleanup", e)
            errors.add("TV show cleanup error: ${e.message}")
            sendCleanupNotification(0, errors, 0L)
        }
    }
    
    private fun sendCleanupNotification(filesDeleted: Int, errors: List<String>, spaceFreedBytes: Long = 0L) {
        try {
            val stats = CleanupStats(
                cleanupType = cleanupType.name,
                filesDeleted = filesDeleted,
                spaceFreeGB = spaceFreedBytes.toDouble() / (1024.0 * 1024.0 * 1024.0),
                dryRun = applicationProperties.dryRun,
                errors = errors
            )
            notificationService.sendCleanupComplete(stats)
        } catch (e: Exception) {
            log.error("Error sending cleanup notification", e)
        }
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