package com.github.schaka.janitorr.mediaserver

data class FavoriteItem(
    val jellyfinId: String,
    val imdbId: String? = null,
    val tmdbId: Int? = null,
    val tvdbId: Int? = null
)
