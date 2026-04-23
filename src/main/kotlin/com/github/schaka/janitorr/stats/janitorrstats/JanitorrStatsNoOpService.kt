package com.github.schaka.janitorr.stats.janitorrstats

import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory

class JanitorrStatsNoOpService : JanitorrStatsService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType) {
        log.info("Janitorr-Stats not enabled - no watch history found.")
    }

}
