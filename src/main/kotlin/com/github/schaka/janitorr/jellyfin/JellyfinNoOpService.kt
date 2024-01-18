package com.github.schaka.janitorr.jellyfin

import com.github.schaka.janitorr.jellyfin.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
 */
@Service
@ConditionalOnProperty("clients.jellyfin.enabled", havingValue = "false", matchIfMissing = true)
class JellyfinNoOpService : JellyfinService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {
        log.info("Jellyfin not implemented. No TV shows deleted.")
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
        log.info("Jellyfin not implemented. No movies deleted.")
    }

    override fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>) {
        log.info("Jellyfin not implemented. No 'Gone Soon' library created.")
    }
}