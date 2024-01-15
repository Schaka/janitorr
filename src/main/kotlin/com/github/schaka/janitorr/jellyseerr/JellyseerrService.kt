package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.servarr.LibraryItem

interface JellyseerrService {
    fun cleanupRequests(items: List<LibraryItem>)
}