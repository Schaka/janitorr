package com.github.schaka.janitorr.stats

import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory

/**
 * Does nothing. Used in case the user does not supply Streamystats configuration.
 */
class StatsNoOpService : StatsService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType) {
        log.info("Streamystats not in use, no requests found.")
    }


}