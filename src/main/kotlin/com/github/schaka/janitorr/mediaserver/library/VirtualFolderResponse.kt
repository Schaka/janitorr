package com.github.schaka.janitorr.mediaserver.library

data class VirtualFolderResponse(
        val CollectionType: String?,
        val ItemId: String?,
        val LibraryOptions: LibraryOptions?,
        val Locations: List<String>,
        val Name: String,
        val PrimaryImageItemId: String?,
        val RefreshProgress: Int,
        val RefreshStatus: String?,

        // Emby only - maybe find a cleaner solution?
        val Id: String?, // equivalent of ItemId
        val Guid: String? // new GUID
)