package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.emby.library.AddVirtualFolder
import com.github.schaka.janitorr.mediaserver.emby.library.LibraryOptions
import com.github.schaka.janitorr.mediaserver.library.*
import com.github.schaka.janitorr.servarr.LibraryItem
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path

open class EmbyRestService(

    @Emby val embyClient: EmbyMediaServerClient,
    @Emby embyUserClient: MediaServerUserClient,
    embyProperties: EmbyProperties,
    applicationProperties: ApplicationProperties,
    fileSystemProperties: FileSystemProperties

) : AbstractMediaServerRestService(
    "Emby",
    embyClient,
    embyUserClient,
    embyProperties,
    applicationProperties,
    fileSystemProperties
) {

    override fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean
    ) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access || fileSystemProperties.leavingSoonDir == null) {
            return
        }

        val result = mediaServerClient.listLibraries()
        val collectionTypeLower = libraryType.collectionType.lowercase()
        // subdirectory (i.e. /leaving-soon/tv/media, /leaving-soon/movies/tag-based
        val path = Path.of(fileSystemProperties.leavingSoonDir, libraryType.folderName, cleanupType.folderName)

        // Clean up library - consider also deleting the collection in Jellyfin/Emby
        if (items.isEmpty() && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            return
        }

        val pathString = path.toUri().path.removeSuffix("/")
        Files.createDirectories(path)
        val libraryName = "${libraryType.collectionName} (Deleted Soon)"

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we just create a media library that consists only
        var leavingSoonCollection = result.firstOrNull { it.CollectionType?.lowercase() == collectionTypeLower && it.Name == libraryName }
        if (leavingSoonCollection == null) {
            // Windows paths may have a trailing trash - Windows Jellyfin/Emby can't deal with that, this is a bit hacky but makes development easier
            val pathForMediaServer = if (pathString.startsWith("/C:")) pathString.replaceFirst("/", "") else pathString
            val libOptions = LibraryOptions(listOf(PathInfo(pathString)), ContentType = collectionTypeLower)
            embyClient.createLibrary(libraryName, collectionTypeLower, AddVirtualFolder(libraryName, collectionTypeLower, listOf(pathForMediaServer), libOptions))
            leavingSoonCollection = mediaServerClient.listLibraries().firstOrNull { it.CollectionType?.lowercase() == collectionTypeLower && it.Name == libraryName }
        }

        // the collection has been found, but maybe our cleanupType specific path hasn't been added to it yet
        val pathSet = leavingSoonCollection?.Locations?.contains(pathString)
        if (pathSet == false) {
            mediaServerClient.addPathToLibrary(AddPathRequest(libraryName, PathInfo(pathString), leavingSoonCollection?.Guid))
        }

        // Clean up entire directory and rebuild from scratch - this can help with clearing orphaned data
        if (fileSystemProperties.fromScratch && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            Files.createDirectories(path)
        }

        createLinks(items, path, libraryType)
    }
}