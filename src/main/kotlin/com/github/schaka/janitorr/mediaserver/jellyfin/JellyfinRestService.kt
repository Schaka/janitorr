package com.github.schaka.janitorr.mediaserver.jellyfin

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.BaseMediaServerService
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.library.*
import com.github.schaka.janitorr.servarr.bazarr.BazarrService

open class JellyfinRestService(

    @Jellyfin jellyfinClient: MediaServerClient,
    @Jellyfin jellyfinUserClient: MediaServerUserClient,
    bazarrService: BazarrService,
    jellyfinProperties: JellyfinProperties,
    applicationProperties: ApplicationProperties,
    fileSystemProperties: FileSystemProperties

) : BaseMediaServerService("Jellyfin", jellyfinClient, jellyfinUserClient, bazarrService, jellyfinProperties, applicationProperties, fileSystemProperties) {

    override fun listLibraries(): List<VirtualFolderResponse> {
        return mediaServerClient.listLibraries()
    }

    override fun createLibrary(libraryName: String, libraryType: LibraryType, pathForMediaServer: String): VirtualFolderResponse {
        val collectionTypeLower = libraryType.collectionType.lowercase()
        mediaServerClient.createLibrary(libraryName, libraryType.collectionType, AddLibraryRequest(), listOf(pathForMediaServer))
        return mediaServerClient.listLibraries().first { it.CollectionType?.lowercase() == collectionTypeLower && it.Name == libraryName }
    }

    override fun addPathToLibrary(leavingSoonCollection: VirtualFolderResponse, pathForMediaServer: String) {
        return mediaServerClient.addPathToLibrary(
            AddPathRequest(leavingSoonCollection.Name, PathInfo(pathForMediaServer))
        )
    }
}