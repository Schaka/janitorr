package com.github.schaka.janitorr.servarr.sonarr.series

data class Statistics(
    val episodeCount: Int,
    val episodeFileCount: Int,
    val nextAiring: String?,
    val percentOfEpisodes: Double,
    val previousAiring: String?,
    val releaseGroups: List<String>,
    val sizeOnDisk: Long,
    val totalEpisodeCount: Int
)