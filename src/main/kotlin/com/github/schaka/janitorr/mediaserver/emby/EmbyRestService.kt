package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.BaseMediaServerService
import com.github.schaka.janitorr.mediaserver.MediaServerLibraryQueryService
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.emby.library.AddMediaPathRequest
import com.github.schaka.janitorr.mediaserver.emby.library.AddVirtualFolder
import com.github.schaka.janitorr.mediaserver.emby.library.LibraryOptions
import com.github.schaka.janitorr.mediaserver.emby.library.PathInfo
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.VirtualFolderResponse
import com.github.schaka.janitorr.servarr.bazarr.BazarrService

open class EmbyRestService(

    @Emby val embyClient: EmbyMediaServerClient,
    @Emby embyUserClient: MediaServerUserClient,
    bazarrService: BazarrService,
    mediaServerLibraryQueryService: MediaServerLibraryQueryService,
    embyProperties: EmbyProperties,
    applicationProperties: ApplicationProperties,
    fileSystemProperties: FileSystemProperties

) : BaseMediaServerService("Emby", embyClient, embyUserClient, bazarrService, mediaServerLibraryQueryService, embyProperties, applicationProperties, fileSystemProperties) {

    override fun listLibraries(): List<VirtualFolderResponse> {
        return embyClient.listLibrariesPage().Items
    }

    override fun createLibrary(libraryName: String, libraryType: LibraryType, pathForMediaServer: String): VirtualFolderResponse {
        val collectionTypeLower = libraryType.collectionType.lowercase()
        val libOptions = LibraryOptions(listOf(PathInfo(pathForMediaServer)), ContentType = collectionTypeLower)
        embyClient.createLibrary(libraryName, collectionTypeLower, AddVirtualFolder(libraryName, collectionTypeLower, listOf(pathForMediaServer), libOptions))
        return embyClient.listLibrariesPage().Items.first { it.CollectionType?.lowercase() == collectionTypeLower && it.Name == libraryName }
    }

    override fun addPathToLibrary(leavingSoonCollection: VirtualFolderResponse, pathForMediaServer: String) {
        embyClient.addPathToLibrary(
            AddMediaPathRequest(
                leavingSoonCollection.Id!!,
                leavingSoonCollection.Guid!!,
                leavingSoonCollection.Name,
                PathInfo(pathForMediaServer)
            )
        )
    }
}