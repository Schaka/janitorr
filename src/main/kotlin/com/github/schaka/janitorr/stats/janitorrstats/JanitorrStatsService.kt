package com.github.schaka.janitorr.stats.janitorrstats

import com.github.schaka.janitorr.stats.StatsService

/**
 * Marker interface for the janitorr-stats fallback. Always wired in,
 * but the active implementation is a NoOp unless the user explicitly enables it.
 */
interface JanitorrStatsService : StatsService
