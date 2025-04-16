package com.github.schaka.janitorr.stats.streamystats

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.stats.StatsService
import com.github.schaka.janitorr.stats.streamystats.requests.StreamystatsHistoryResponse
import com.github.schaka.janitorr.stats.streamystats.requests.WatchHistoryEntry
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Does nothing. Used in case the user does not supply Jellyfin configuration.
 */
class StreamystatsRestService(
    val streamystatsClient: StreamystatsClient,
    val streamystatsProperties: StreamystatsProperties,
    val mediaServerService: AbstractMediaServerService,
    val applicationProperties: ApplicationProperties
) : StatsService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType) {
        // populates per season, per show or per movie, depending on properties
        // e.g. each season item can be populated with its TV show mediaserver id
        // watch age is determined by the matched mediaserver id
        // TODO: find a better way - passing properties to an unrelated component couples them unnecessarily
        mediaServerService.populateMediaServerIds(items, type, streamystatsProperties)

        // TODO: if at all possible, we shouldn't populate the list with media server ids differently, but recognize a season and treat show as a whole as per application properties
        // example: grab show id for season id, get watchHistory based on show instead of season

        for (item in items.filter { it.mediaServerIds.isNotEmpty() }) {
            // every movie, show, season and episode has its own unique ID, so every request will only consider what's passed to it here
            val response = item.mediaServerIds.map(streamystatsClient::getRequests)

            val watchHistory = response
                .filter { it.statistics.lastWatched != null }
                .flatMap { it.statistics.watchHistory }
                .filter { it.playDuration > 60 }
                .maxByOrNull { toDate(it.startTime) } // most recent date

            // only count view if at least one minute of content was watched - could be user adjustable later
            if (watchHistory != null) {
                item.lastSeen = toDate(watchHistory.startTime)
                logWatchInfo(item, watchHistory, response[0])
            }

        }

        if (log.isTraceEnabled) {
            for (item in items.filter { it.mediaServerIds.isEmpty() }) {
                log.trace("Could not find any matching media server id for ${item.filePath} IMDB: ${item.imdbId} TMDB: ${item.tmdbId} TVDB: ${item.tvdbId} Season: ${item.season}")
            }
        }
    }

    private fun logWatchInfo(item: LibraryItem, watchHistory: WatchHistoryEntry?, response: StreamystatsHistoryResponse?) {
        if (response?.item?.type == "Season") {
            val season = "${response.item.seriesName} ${response.item.name}"
            log.debug("Updating history - user {} watched {} at {}", watchHistory?.userName, season, watchHistory?.startTime)
        } else {
            log.debug("Updating history - user {} watched {} at {}", watchHistory?.userName, response?.item?.name, watchHistory?.startTime)
        }
    }

    private fun toDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date.split("T")[0])
    }

}