package com.github.schaka.janitorr

import com.github.schaka.janitorr.jellyfin.JellyfinService
import com.github.schaka.janitorr.jellyfin.library.LibraryType
import com.github.schaka.janitorr.jellyseerr.JellyseerrRestService
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
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

        val radarrMovies = radarrService.getEntries()
        val leavingSoonMovies = radarrMovies.filter { it.date.plusDays(movieExpiration - leavingSoonExpiration) < today && it.date.plusDays(movieExpiration) >= today }
        jellyfinService.updateGoneSoon(LibraryType.MOVIES, leavingSoonMovies)

        val sonarrShows = sonarrService.getEntries()
        val leavingShows = sonarrShows.filter { it.date.plusDays(seasonExpiration - leavingSoonExpiration) < today && it.date.plusDays(seasonExpiration) >= today }
        jellyfinService.updateGoneSoon(LibraryType.TV_SHOWS, leavingShows)

        val toDeleteShows = sonarrShows.filter { it.date.plusDays(seasonExpiration) < today }
        sonarrService.removeEntries(toDeleteShows)
        jellyseerrService.cleanupRequests(toDeleteShows)
        jellyfinService.cleanupTvShows(toDeleteShows)

        val toDeleteMovies = radarrMovies.filter { it.date.plusDays(movieExpiration) < today }
        radarrService.removeEntries(toDeleteMovies)
        jellyseerrService.cleanupRequests(toDeleteMovies)
        jellyfinService.cleanupMovies(toDeleteMovies)
    }
}