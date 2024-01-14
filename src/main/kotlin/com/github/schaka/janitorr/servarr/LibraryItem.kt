package com.github.schaka.janitorr.servarr

import java.time.LocalDateTime

data class LibraryItem(
        val id: Int,
        val date: LocalDateTime,
        val originalPath: String,
        val libraryPath: String,
        val fullPath: String, // points to a season or a movie, rather than a single episode, trailer, etc

        val imdbId: String? = null,
        val tvdbId: Int? = null,
        val tmdbId: Int? = null,
        val season: Int? = null,

)
