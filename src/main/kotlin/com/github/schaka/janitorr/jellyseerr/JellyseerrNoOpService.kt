package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.jellyfin.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
 */
@Service
@ConditionalOnProperty("clients.jellyseerr.enabled", havingValue = "false", matchIfMissing = true)
class JellyseerrNoOpService : JellyseerrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupRequests(items: List<LibraryItem>) {
        log.info("Jellyseer not in use, no requests found.")
    }


}