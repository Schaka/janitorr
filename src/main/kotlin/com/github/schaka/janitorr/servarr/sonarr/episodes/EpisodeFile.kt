package com.github.schaka.janitorr.servarr.sonarr.episodes

data class EpisodeFile(
        val id: Int,
        val seriesId: Int,
        val seasoNumber: Int,
        val relativePath: String,
        val path: String
)
