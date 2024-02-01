package com.github.schaka.janitorr

import com.github.schaka.janitorr.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.jellyfin.JellyfinService
import com.github.schaka.janitorr.jellyfin.library.LibraryType
import com.github.schaka.janitorr.jellyseerr.JellyseerrRestService
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
        val schedule: CleanupSchedule
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @GetMapping("/test")
    fun sonarr(): ResponseEntity<Any> {
        schedule.runSchedule()
        return ResponseEntity.noContent().build()
    }
}