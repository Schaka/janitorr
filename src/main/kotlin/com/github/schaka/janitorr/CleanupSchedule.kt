package com.github.schaka.janitorr

import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.radarr.Radarr
import com.github.schaka.janitorr.servarr.sonarr.Sonarr
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.time.Duration
import java.time.LocalDateTime

@Service
class CleanupSchedule(
        val mediaServerService: MediaServerService,
        val jellyseerrService: JellyseerrService,
        val fileSystemProperties: FileSystemProperties,
        val applicationProperties: ApplicationProperties,
        @Sonarr
        val sonarrService: ServarrService,
        @Radarr
        val radarrService: ServarrService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    // run every hour
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun runSchedule() {

        val seasonExpiration = determineDeletion(applicationProperties.seasonExpiration)
        val movieExpiration = determineDeletion(applicationProperties.movieExpiration)

        cleanupMediaType(TV_SHOWS, sonarrService, seasonExpiration, this::deleteTvShows)
        cleanupMediaType(MOVIES, radarrService, movieExpiration, this::deleteMovies)
    }

    /**
     * Convert to full days and do some math.
     * This should probably work just letting the user set the duration entirely. But I think forcing full days will avoid some user errors.
     */
    private fun cleanupMediaType(type: LibraryType, servarrService: ServarrService, expiration: Duration?, deleteTask: (list: List<LibraryItem>) -> Unit) {

        if (expiration == null) {
            log.info("Not deleting ${type.collectionName} because minimum disk threshold was not reached.")
            return
        }

        val today = LocalDateTime.now()
        val leavingSoonExpiration = applicationProperties.leavingSoon.toDays()
        val expirationDays = expiration.toDays()

        val servarrEntries = servarrService.getEntries()
        val leavingSoon = servarrEntries.filter { it.date.plusDays(expirationDays - leavingSoonExpiration) < today && it.date.plusDays(expirationDays) >= today }
        mediaServerService.updateGoneSoon(type, leavingSoon)

        val toDeleteMedia = servarrEntries.filter { it.date.plusDays(expirationDays) < today }
        deleteTask(toDeleteMedia)
    }

    private fun determineDeletion(deletionConditions: Map<Int, Duration>): Duration? {

        // If we don't have access to the same file system as the library, we can't determine the actual space left and will just choose the longest expiration time available
        if (!fileSystemProperties.access) {
            return deletionConditions.entries.maxByOrNull { it.value.toDays() }?.value
        }

        val filesystem = File("/")
        val freeSpacePercentage = (filesystem.freeSpace.toDouble() / filesystem.totalSpace.toDouble()) * 100

        val entry = deletionConditions.entries.filter { freeSpacePercentage < it.key }.minByOrNull { it.key }
        return entry?.value
    }

    private fun deleteMovies(toDeleteMovies: List<LibraryItem>) {
        radarrService.removeEntries(toDeleteMovies)

        val cannotDeleteMovies = toDeleteMovies.filter { it.seeding }
        val deletedMovies = toDeleteMovies.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedMovies)
        mediaServerService.cleanupMovies(deletedMovies)
        mediaServerService.updateGoneSoon(MOVIES, cannotDeleteMovies, true)
    }

    private fun deleteTvShows(toDeleteShows: List<LibraryItem>) {
        sonarrService.removeEntries(toDeleteShows)

        val cannotDeleteShow = toDeleteShows.filter { it.seeding }
        val deletedShows = toDeleteShows.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedShows)
        mediaServerService.cleanupTvShows(deletedShows)
        mediaServerService.updateGoneSoon(TV_SHOWS, cannotDeleteShow, true)
    }

}