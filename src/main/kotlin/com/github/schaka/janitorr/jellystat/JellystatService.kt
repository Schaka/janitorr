package com.github.schaka.janitorr.jellystat

import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem

interface JellystatService {

    fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType)

}