package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
 */
@Service
@ConditionalOnProperty(value = ["clients.emby.enabled", "clients.jellyfin.enabled"], havingValue = "false")
class MediaServerNoOpService : MediaServerService() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {
        log.info("Media Server not implemented. No TV shows deleted.")
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
        log.info("Media Server not implemented. No movies deleted.")
    }

    override fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {
        log.info("Media Server not implemented. No 'Gone Soon' library created.")
    }
}