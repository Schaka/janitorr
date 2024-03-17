package com.github.schaka.janitorr.servarr.sonarr.series

data class StatisticsX(
        val episodeCount: Int,
        val episodeFileCount: Int,
        val percentOfEpisodes: Double,
        val releaseGroups: List<String>,
        val seasonCount: Int,
        val sizeOnDisk: Long,
        val totalEpisodeCount: Int
)