package com.github.schaka.janitorr

import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.radarr.Radarr
import com.github.schaka.janitorr.servarr.sonarr.Sonarr
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CleanupSchedule(
        val mediaServerService: MediaServerService,
        val jellyseerrService: JellyseerrService,
        val applicationProperties: ApplicationProperties,
        @Sonarr
        val sonarrService: ServarrService,
        @Radarr
        val radarrService: ServarrService,
) {

    // run every hour
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun runSchedule() {

        val today = LocalDateTime.now()
        val seasonExpiration = applicationProperties.seasonExpiration.toDays()
        val movieExpiration = applicationProperties.movieExpiration.toDays()
        val leavingSoonExpiration = applicationProperties.leavingSoon.toDays()

        val sonarrShows = sonarrService.getEntries()
        val leavingShows = sonarrShows.filter { it.date.plusDays(seasonExpiration - leavingSoonExpiration) < today && it.date.plusDays(seasonExpiration) >= today }
        mediaServerService.updateGoneSoon(TV_SHOWS, leavingShows)

        val toDeleteShows = sonarrShows.filter { it.date.plusDays(seasonExpiration) < today }
        deleteTvShows(toDeleteShows)

        val radarrMovies = radarrService.getEntries()
        val leavingSoonMovies = radarrMovies.filter { it.date.plusDays(movieExpiration - leavingSoonExpiration) < today && it.date.plusDays(movieExpiration) >= today }
        mediaServerService.updateGoneSoon(MOVIES, leavingSoonMovies)

        val toDeleteMovies = radarrMovies.filter { it.date.plusDays(movieExpiration) < today }
        deleteMovies(toDeleteMovies)
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