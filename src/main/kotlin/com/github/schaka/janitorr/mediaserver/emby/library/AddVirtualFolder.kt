package com.github.schaka.janitorr.mediaserver.emby.library

import com.github.schaka.janitorr.mediaserver.library.LibraryOptions

data class AddVirtualFolder(
        val Name: String,
        val CollectionType: String,
        val Paths: List<String>?,
        val LibraryOptions: LibraryOptions? = null
)