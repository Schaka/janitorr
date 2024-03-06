package com.github.schaka.janitorr.mediaserver.jellyfin.library

data class VirtualFolderResponse(
    val CollectionType: String?,
    val ItemId: String?,
    val LibraryOptions: LibraryOptions?,
    val Locations: List<String>,
    val Name: String,
    val PrimaryImageItemId: String?,
    val RefreshProgress: Int,
    val RefreshStatus: String?
)