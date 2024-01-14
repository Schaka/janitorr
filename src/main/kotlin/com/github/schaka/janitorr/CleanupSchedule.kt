package com.github.schaka.janitorr

import com.github.schaka.janitorr.jellyfin.JellyfinService
import com.github.schaka.janitorr.jellyfin.library.LibraryType
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

        var radarrMovies = radarrService.getEntries()
        jellyfinService.updateGoneSoon(LibraryType.MOVIES, radarrMovies)

        var sonarrShows = sonarrService.getEntries()
        jellyfinService.updateGoneSoon(LibraryType.TV_SHOWS, sonarrShows)

        val today = LocalDateTime.now()
        val toDeleteShows = sonarrShows.filter { it.date.plusDays(applicationProperties.seasonExpiration.toDays()) < today }
        sonarrService.removeEntries(toDeleteShows)
        jellyseerrService.cleanupRequests(toDeleteShows)
        jellyfinService.cleanupTvShows(toDeleteShows)

        val toDeleteMovies = radarrMovies.filter { it.date.plusDays(applicationProperties.movieExpiration.toDays()) < today }
        radarrService.removeEntries(toDeleteMovies)
        jellyseerrService.cleanupRequests(toDeleteMovies)
        jellyfinService.cleanupMovies(toDeleteMovies)
    }
}