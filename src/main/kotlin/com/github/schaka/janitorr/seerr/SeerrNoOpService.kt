package com.github.schaka.janitorr.seerr

import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Does nothing. Used in case the user does not supply Seerr configuration.
 */
@ConditionalOnProperty(name = ["clients.jellyseerr.enabled"], havingValue = "false", matchIfMissing = true)
@Service
class SeerrNoOpService : SeerrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupRequests(items: List<LibraryItem>) {
        log.info("Seerr not in use, no requests found.")
    }

}
