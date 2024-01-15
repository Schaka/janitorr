package com.github.schaka.janitorr.jellyfin

import com.github.schaka.janitorr.jellyfin.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem

interface JellyfinService {
    fun cleanupTvShows(items: List<LibraryItem>)

    fun cleanupMovies(items: List<LibraryItem>)

    fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>)
}
