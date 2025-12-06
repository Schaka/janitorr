package com.github.schaka.janitorr.mediaserver.config

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.stats.jellystat.JellystatProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.FavoriteItem
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.stats.StatsClientProperties
import org.slf4j.LoggerFactory

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
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

    override fun populateMediaServerIds(items: List<LibraryItem>, type: LibraryType, config: StatsClientProperties) {
        log.info("Media Server not implemented. No server IDs populated.")
    }

    override fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {
        log.info("Media Server not implemented. No 'Leaving Soon' library created.")
    }

    override fun getAllFavoritedItems(): List<FavoriteItem> {
        return emptyList()
    }

    override fun isItemFavorited(item: LibraryItem, favoritedItems: List<FavoriteItem>): Boolean {
        return false
    }

    override fun filterOutFavorites(items: List<LibraryItem>, libraryType: LibraryType): List<LibraryItem> {
        return items
    }
}