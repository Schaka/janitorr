package com.github.schaka.janitorr.mediaserver.emby.library

data class AddVirtualFolder(
        val Name: String,
        val CollectionType: String,
        val Paths: List<String>?,
        val LibraryOptions: LibraryOptions? = null
)