package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem

interface MediaServerService {
    fun cleanupTvShows(items: List<LibraryItem>)

    fun cleanupMovies(items: List<LibraryItem>)

    fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean = false)
}
