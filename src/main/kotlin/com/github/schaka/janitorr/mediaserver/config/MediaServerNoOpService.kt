package com.github.schaka.janitorr.mediaserver.config

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.lookup.MediaLookup
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory

/**
 * Does nothing. Used in case the user does not supply Jellyfin/Emby configuration.
 */
class MediaServerNoOpService : AbstractMediaServerService() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {
        log.info("Media Server not implemented. No TV shows deleted.")
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
        log.info("Media Server not implemented. No movies deleted.")
    }

    override fun populateMediaServerIds(
        items: List<LibraryItem>,
        type: LibraryType,
        bySeason: Boolean
    ) {
        log.info("Media Server not implemented. No server IDs populated.")
    }

    override fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {
        log.info("Media Server not implemented. No 'Leaving Soon' library created.")
    }

    override fun getMediaServerIdsForLibrary(
        items: List<LibraryItem>,
        type: LibraryType,
        bySeason: Boolean
    ): Map<MediaLookup, List<String>> {
        log.info("Media Server not implemented. No mediaServerIds populated.")
        return mapOf()
    }

    override fun getAllFavoritedItems(): List<LibraryContent> {
        return emptyList()
    }

    override fun filterOutFavorites(items: List<LibraryItem>, libraryType: LibraryType): List<LibraryItem> {
        return items
    }
}