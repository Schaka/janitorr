package com.github.schaka.janitorr.stats.janitorrstats

import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.stats.janitorrstats.requests.JanitorrStatsPagedResponse
import com.github.schaka.janitorr.stats.janitorrstats.requests.JanitorrStatsPlayEvent
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class JanitorrStatsRestService(
    val janitorrStatsClient: JanitorrStatsClient,
) : JanitorrStatsService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType) {
        for (item in items) {
            val mostRecent = gracefulQuery(item, type) ?: continue
            val playedAt = LocalDateTime.ofInstant(Instant.parse(mostRecent.playedAt), ZoneOffset.UTC)
            item.lastSeen = item.lastSeen?.let { if (playedAt.isAfter(it)) playedAt else it } ?: playedAt
            log.debug("janitorr-stats history - user {} watched {} (season {}) at {} UTC", mostRecent.username, item.id, item.season, mostRecent.playedAt)
        }
    }

    private fun gracefulQuery(item: LibraryItem, type: LibraryType): JanitorrStatsPlayEvent? {
        return try {
            queryByBestAvailableId(item, type).content
                .filter { it.durationMs > 60_000 }
                .maxByOrNull { it.playedAt }
        } catch (e: Exception) {
            if (log.isTraceEnabled) {
                log.warn("janitorr-stats lookup failed for item {} (season {})", item.id, item.season, e)
            } else {
                log.warn("janitorr-stats lookup failed for item {} (season {})", item.id, item.season)
            }
            null
        }
    }

    private fun queryByBestAvailableId(item: LibraryItem, type: LibraryType): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent> {
        if (type == LibraryType.TV_SHOWS) {
            return queryShow(item)
        }
        return queryMovie(item)
    }

    private fun queryMovie(item: LibraryItem): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent> {
        val imdbId = item.imdbId
        val tmdbId = item.tmdbId
        return when {
            imdbId != null && tmdbId != null -> janitorrStatsClient.getMovieHistory(imdbId, tmdbId)
            imdbId != null -> janitorrStatsClient.getMovieHistoryByImdb(imdbId)
            tmdbId != null -> janitorrStatsClient.getMovieHistoryByTmdb(tmdbId)
            else -> JanitorrStatsPagedResponse()
        }
    }

    private fun queryShow(item: LibraryItem): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent> {
        val tvdbId = item.tvdbId
        val imdbId = item.imdbId
        val tmdbId = item.tmdbId
        val season = item.season
        return when {
            tvdbId != null && season != null -> janitorrStatsClient.getShowHistoryByTvdb(tvdbId, season)
            tvdbId != null -> janitorrStatsClient.getShowHistoryByTvdb(tvdbId)
            imdbId != null && season != null -> janitorrStatsClient.getShowHistoryByImdb(imdbId, season)
            imdbId != null -> janitorrStatsClient.getShowHistoryByImdb(imdbId)
            tmdbId != null && season != null -> janitorrStatsClient.getShowHistoryByTmdb(tmdbId, season)
            tmdbId != null -> janitorrStatsClient.getShowHistoryByTmdb(tmdbId)
            else -> JanitorrStatsPagedResponse()
        }
    }

}
