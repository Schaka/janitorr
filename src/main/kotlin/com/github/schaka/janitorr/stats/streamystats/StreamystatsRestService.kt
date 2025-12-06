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
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

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
        // if WatchHistory settings require a different way of aggregating MediaServerIds, request them again
        // TODO: if at all possible, we shouldn't populate the list of media server ids differently, but recognize a season and treat show as a whole as per application properties
        // example: grab show id for season id, get WatchHistory based on show instead of season
        if (applicationProperties.wholeTvShow != streamystatsProperties.wholeTvShow) {
            mediaServerService.populateMediaServerIds(items, type, !streamystatsProperties.wholeTvShow)
        }

        for (item in items.filter { it.mediaServerIds.isNotEmpty() }) {
            // every movie, show, season and episode has its own unique ID, so every request will only consider what's passed to it here
            val response = item.mediaServerIds.map(::gracefulQuery)

            val watchHistory = response
                .filter { it != null && it.lastWatched != null }
                .flatMap { it!!.watchHistory }
                .filter { it.watchDuration > 60 }
                .maxByOrNull { toDate(it.watchDate) } // most recent date

            // only count view if at least one minute of content was watched - could be user adjustable later
            if (watchHistory != null) {
                item.lastSeen = toDate(watchHistory.watchDate)
                logWatchInfo(item, watchHistory, response[0])
            }

        }

        if (log.isTraceEnabled) {
            for (item in items.filter { it.mediaServerIds.isEmpty() }) {
                log.trace("Could not find any matching media server id for ${item.filePath} IMDB: ${item.imdbId} TMDB: ${item.tmdbId} TVDB: ${item.tvdbId} Season: ${item.season}")
            }
        }
    }

    private fun gracefulQuery(jellyfinId: String): StreamystatsHistoryResponse? {
        try {
            return streamystatsClient.getRequests(jellyfinId)
        } catch (e: Exception) {
            if (log.isTraceEnabled) {
                log.warn("Stats via Streamystats not found for Jellyfin ID: {}", jellyfinId)
            } else {
                log.warn("Stats via Streamystats not found for Jellyfin ID: {}", jellyfinId, e)
            }
        }
        return null
    }

    private fun logWatchInfo(item: LibraryItem, watchHistory: WatchHistoryEntry?, response: StreamystatsHistoryResponse?) {
        if (response?.item?.type == "Season") {
            val season = "${response.item.seriesName} ${response.item.name}"
            log.debug("Updating history - user {} watched {} at {}", watchHistory?.user?.name, season, watchHistory?.watchDate)
        } else {
            log.debug("Updating history - user {} watched {} at {}", watchHistory?.user?.name, response?.item?.name, watchHistory?.watchDate)
        }
    }

    private fun toDate(date: String): LocalDateTime {
        // 2025-04-16T05:27:15Z
        return LocalDateTime.parse(date.substring(0, date.length - 1), ISO_LOCAL_DATE_TIME)
    }

}