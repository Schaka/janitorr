package com.github.schaka.janitorr.jellystat

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.jellystat.requests.ItemRequest
import com.github.schaka.janitorr.jellystat.requests.WatchHistoryResponse
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
 */
class JellystatRestService(
    val jellystatClient: JellystatClient,
    val jellystatProperties: JellystatProperties,
    val mediaServerService: AbstractMediaServerService,
    val applicationProperties: ApplicationProperties
) : JellystatService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType) {
        // populates per season, per show or per movie, depending on properties
        // e.g. each season item can be populated with its TV show mediaserver id
        // watch age is determined by the matched mediaserver id
        // TODO: find a better way - passing properties to an unrelated component couples them unnecessarily
        mediaServerService.populateMediaServerIds(items, type, jellystatProperties)

        // TODO: if at all possible, we shouldn't populate the list with media server ids differently, but recognize a season and treat show as a whole as per application properties
        // example: grab show id for season id, get watchHistory based on show instead of season

        for (item in items.filter { it.mediaServerIds.isNotEmpty() }) {
            // every movie, show, season and episode has its own unique ID, so every request will only consider what's passed to it here
            val watchHistory = item.mediaServerIds
                .asSequence()
                .map(::ItemRequest)
                .map(jellystatClient::getRequests)
                .flatMap { page -> page.results }
                .filter { it.PlaybackDuration > 60 }
                .maxByOrNull { toDate(it.ActivityDateInserted) } // most recent date

            // only count view if at least one minute of content was watched - could be user adjustable later
            if (watchHistory != null) {
                item.lastSeen = toDate(watchHistory.ActivityDateInserted)
                logWatchInfo(item, watchHistory)
            }

        }

        if (log.isTraceEnabled) {
            for (item in items.filter { it.mediaServerIds.isEmpty() }) {
                log.trace("Could not find any matching media server id for ${item.filePath} IMDB: ${item.imdbId} TMDB: ${item.tmdbId} TVDB: ${item.tvdbId} Season: ${item.season}")
            }
        }
    }

    private fun logWatchInfo(item: LibraryItem, watchHistory: WatchHistoryResponse?) {
        if (watchHistory?.SeasonId != null) {
            val season = "${watchHistory.NowPlayingItemName} ${item.season}"
            log.debug("Updating history - user {} watched {} at {}", watchHistory.UserName, season, watchHistory.ActivityDateInserted)
        } else {
            log.debug("Updating history - user {} watched {} at {}", watchHistory?.UserName, watchHistory?.NowPlayingItemName, watchHistory?.ActivityDateInserted)
        }
    }

    private fun toDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date.substring(0, date.length - 1))
    }

}