package com.github.schaka.janitorr.jellyfin.library

enum class LibraryType(
        val collectionType: String?,
        val collectionName: String)
{

    MOVIES("Movies", "Movies"),
    TV_SHOWS("TvShows", "Shows")

}