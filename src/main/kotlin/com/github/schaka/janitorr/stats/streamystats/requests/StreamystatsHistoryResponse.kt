package com.github.schaka.janitorr.stats.streamystats.requests

data class StreamystatsHistoryResponse(
        val item: WatchHistoryItem,
        val statistics: WatchHistoryStatistics
)
