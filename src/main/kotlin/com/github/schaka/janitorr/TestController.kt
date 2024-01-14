package com.github.schaka.janitorr

import com.github.schaka.janitorr.jellyfin.JellyfinService
import com.github.schaka.janitorr.jellyfin.library.LibraryType
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.servarr.radarr.RadarrService
import com.github.schaka.janitorr.servarr.sonarr.SonarrService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDateTime

@Controller
@RequestMapping("/hook")
class TestController(
        val jellyfinService: JellyfinService,
        val jellyseerrService: JellyseerrService,
        val applicationProperties: ApplicationProperties,
        val sonarrService: SonarrService,
        val radarrService: RadarrService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @GetMapping("/test")
    fun sonarr(): ResponseEntity<Any> {

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

        return ResponseEntity(sonarrShows, HttpStatus.OK)
    }
}