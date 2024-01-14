package com.github.schaka.janitorr.servarr.sonarr.episodes

data class EpisodeResponse(
        val id: Int,
        val seriesId: Int,
        val tvdbId: Int?,
        val episodeFileId: Int?,
        val seasonNumber: Int,
        val episodeNumber: Int,
)
