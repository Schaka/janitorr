package com.github.schaka.janitorr.mediaserver.config

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.jellystat.JellystatProperties
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
 */
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

    override fun populateMediaServerIds(items: List<LibraryItem>, type: LibraryType, config: JellystatProperties) {
        log.info("Media Server not implemented. No server IDs populated.")
    }

    override fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {
        log.info("Media Server not implemented. No 'Leaving Soon' library created.")
    }
}