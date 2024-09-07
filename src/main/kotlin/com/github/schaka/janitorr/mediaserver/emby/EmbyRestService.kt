package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService.Companion
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.emby.library.*
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
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

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val windowsRegex = Regex("/\\w:.*")
    }

    override fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean
    ) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access || fileSystemProperties.leavingSoonDir == null) {
            return
        }

        val result = embyClient.listLibrariesPage().Items
        val collectionTypeLower = libraryType.collectionType.lowercase()
        // subdirectory (i.e. /leaving-soon/tv/media, /leaving-soon/movies/tag-based
        val path = Path.of(fileSystemProperties.leavingSoonDir, libraryType.folderName, cleanupType.folderName)
        val mediaServerPath = Path.of(fileSystemProperties.mediaServerLeavingSoonDir ?: fileSystemProperties.leavingSoonDir, libraryType.folderName, cleanupType.folderName)

        // Clean up library - consider also deleting the collection in Jellyfin/Emby
        if (items.isEmpty() && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            return
        }

        val pathString = mediaServerPath.toUri().path.removeSuffix("/")
        // Windows paths may have a trailing trash - Windows Jellyfin/Emby can't deal with that, this is a bit hacky but makes development easier
        val pathForMediaServer = if (windowsRegex.matches(pathString)) pathString.replaceFirst("/", "") else pathString

        Files.createDirectories(path)
        val libraryName = "${libraryType.collectionName} (Deleted Soon)"

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we just create a media library that consists only
        var leavingSoonCollection = result.firstOrNull { it.CollectionType?.lowercase() == collectionTypeLower && it.Name == libraryName }
        if (leavingSoonCollection == null) {
            val libOptions = LibraryOptions(listOf(PathInfo(pathString)), ContentType = collectionTypeLower)
            embyClient.createLibrary(libraryName, collectionTypeLower, AddVirtualFolder(libraryName, collectionTypeLower, listOf(pathForMediaServer), libOptions))
            leavingSoonCollection = embyClient.listLibrariesPage().Items.firstOrNull { it.CollectionType?.lowercase() == collectionTypeLower && it.Name == libraryName }
        }

        log.trace("Leaving Soon Collection Created/Found: {}", leavingSoonCollection)

        // the collection has been found, but maybe our cleanupType specific path hasn't been added to it yet
        val pathSet = leavingSoonCollection?.Locations?.contains(pathString)
        if (pathSet == false) {
                embyClient.addPathToLibrary(
                    AddMediaPathRequest(
                        leavingSoonCollection?.Id!!,
                        leavingSoonCollection.Guid!!,
                        leavingSoonCollection.Name,
                        PathInfo(pathString)
                    )
                )
        }

        // Clean up entire directory and rebuild from scratch - this can help with clearing orphaned data
        if (fileSystemProperties.fromScratch && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            Files.createDirectories(path)
        }

        createLinks(items, path, libraryType)
    }
}