package com.github.schaka.janitorr.seerr

import com.github.schaka.janitorr.servarr.LibraryItem

interface SeerrService {
    fun cleanupRequests(items: List<LibraryItem>)
}
