package com.github.schaka.janitorr.stats

import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem

interface StatsService {

    fun populateWatchHistory(items: List<LibraryItem>, type: LibraryType)

}