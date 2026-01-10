package com.github.schaka.janitorr.stats.jellystat

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.lookup.MediaLookup
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.stats.StatsService
import com.github.schaka.janitorr.stats.jellystat.requests.JellyStatHistoryResponse
import com.github.schaka.janitorr.stats.jellystat.requests.JellystatItemRequest
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class JellystatRestService(
    val jellystatClient: JellystatClient,
    val jellystatProperties: JellystatProperties,
    val mediaServerService: AbstractMediaServerService,
    val applicationProperties: ApplicationProperties
) : StatsService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType) {

        // TODO: if at all possible, we shouldn't populate the list of media server ids differently, but recognize a season and treat show as a whole as per application properties
        // example: grab show id for season id, get WatchHistory based on show instead of season
        val libraryMappings = mediaServerService.getMediaServerIdsForLibrary(items, type, !jellystatProperties.wholeTvShow)

        for (item in items) {
            // every movie, show, season and episode has its own unique ID, so every request will only consider what's passed to it here
            val lookupKey = if (type == LibraryType.TV_SHOWS && !jellystatProperties.wholeTvShow) MediaLookup(item.id, item.season) else MediaLookup(item.id)
            val watchHistory = libraryMappings.getOrDefault(lookupKey, listOf())
                .map(::JellystatItemRequest)
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
    }

    private fun logWatchInfo(item: LibraryItem, watchHistory: JellyStatHistoryResponse?) {
        if (watchHistory?.SeasonId != null) {
            val season = "${watchHistory.NowPlayingItemName} Season ${item.season}"
            log.debug("Updating history - user {} watched {} at {}", watchHistory.UserName, season, watchHistory.ActivityDateInserted)
        } else {
            log.debug("Updating history - user {} watched {} at {}", watchHistory?.UserName, watchHistory?.NowPlayingItemName, watchHistory?.ActivityDateInserted)
        }
    }

    private fun toDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date.dropLast(1))
    }

}