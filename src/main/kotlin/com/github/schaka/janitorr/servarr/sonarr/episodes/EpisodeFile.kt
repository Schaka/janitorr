package com.github.schaka.janitorr.servarr.sonarr.episodes

data class EpisodeFile(
        val id: Int,
        val seriesId: Int,
        val seasonNumber: Int,
        val relativePath: String,
        val path: String,
        val size: Long? = null
)
