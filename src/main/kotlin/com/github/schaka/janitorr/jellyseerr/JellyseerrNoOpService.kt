package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
 */
class JellyseerrNoOpService : JellyseerrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupRequests(items: List<LibraryItem>) {
        log.info("Jellyseer not in use, no requests found.")
    }


}