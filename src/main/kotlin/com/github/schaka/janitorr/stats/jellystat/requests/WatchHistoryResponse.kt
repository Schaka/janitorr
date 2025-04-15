package com.github.schaka.janitorr.stats.jellystat.requests

data class WatchHistoryResponse(
        val Id: String,
        val ActivityDateInserted: String,
        val NowPlayingItemName: String?,
        val PlaybackDuration: Int, // Seconds
        val SeasonId: String?,
        val SeriesName: String?,
        val UserName: String?,
        val Client: String?,
)
