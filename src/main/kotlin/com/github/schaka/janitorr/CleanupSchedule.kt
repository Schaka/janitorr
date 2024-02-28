package com.github.schaka.janitorr

import com.github.schaka.janitorr.jellyfin.JellyfinService
import com.github.schaka.janitorr.jellyfin.library.LibraryType
import com.github.schaka.janitorr.jellyfin.library.LibraryType.MOVIES
import com.github.schaka.janitorr.jellyfin.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.jellyseerr.JellyseerrRestService
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.radarr.RadarrService
import com.github.schaka.janitorr.servarr.sonarr.SonarrService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CleanupSchedule(
        val jellyfinService: JellyfinService,
        val jellyseerrService: JellyseerrService,
        val applicationProperties: ApplicationProperties,
        val sonarrService: SonarrService,
        val radarrService: RadarrService,
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
        jellyfinService.updateGoneSoon(TV_SHOWS, leavingShows)

        val toDeleteShows = sonarrShows.filter { it.date.plusDays(seasonExpiration) < today }
        deleteTvShows(toDeleteShows)

        val radarrMovies = radarrService.getEntries()
        val leavingSoonMovies = radarrMovies.filter { it.date.plusDays(movieExpiration - leavingSoonExpiration) < today && it.date.plusDays(movieExpiration) >= today }
        jellyfinService.updateGoneSoon(MOVIES, leavingSoonMovies)

        val toDeleteMovies = radarrMovies.filter { it.date.plusDays(movieExpiration) < today }
        deleteMovies(toDeleteMovies)
    }

    private fun deleteMovies(toDeleteMovies: List<LibraryItem>) {
        radarrService.removeEntries(toDeleteMovies)

        val cannotDeleteMovies = toDeleteMovies.filter { it.seeding }
        val deletedMovies = toDeleteMovies.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedMovies)
        jellyfinService.cleanupMovies(deletedMovies)
        jellyfinService.updateGoneSoon(MOVIES, cannotDeleteMovies, true)
    }

    private fun deleteTvShows(toDeleteShows: List<LibraryItem>) {
        sonarrService.removeEntries(toDeleteShows)

        val cannotDeleteShow = toDeleteShows.filter { it.seeding }
        val deletedShows = toDeleteShows.filter { !it.seeding }

        jellyseerrService.cleanupRequests(deletedShows)
        jellyfinService.cleanupTvShows(deletedShows)
        jellyfinService.updateGoneSoon(TV_SHOWS, cannotDeleteShow, true)
    }
}