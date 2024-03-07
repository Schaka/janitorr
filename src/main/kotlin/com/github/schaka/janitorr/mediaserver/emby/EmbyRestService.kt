package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.mediaserver.jellyfin.library.AddLibraryRequest
import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

@Service
@ConditionalOnProperty("clients.emby.enabled", havingValue = "true", matchIfMissing = false)
class EmbyRestService(

        val embyProperties: EmbyProperties,
        val applicationProperties: ApplicationProperties,
        val fileSystemProperties: FileSystemProperties,
        val embyClient: EmbyClient,
        val embyUserClient: EmbyUserClient

) : MediaServerService() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
    }

    override fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access || fileSystemProperties.leavingSoonDir == null) {
            return
        }

        val result = embyClient.listLibraries()
        val collectionFilter = type.collectionType.lowercase()
        // subdirectory (i.e. /leaving-soon/tv
        val path = Path.of(fileSystemProperties.leavingSoonDir, type.folderName)

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we just create a media library that consists only
        var goneSoonCollection = result.firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        if (goneSoonCollection == null) {
            Files.createDirectories(path)
            val pathString = path.toUri().path
            // Windows paths may have a trailing trash - Windows Emby can't deal with that, this is a bit hacky but makes development easier
            val pathForEmby = if (pathString.startsWith("/C:")) pathString.replaceFirst("/", "") else pathString
            embyClient.createLibrary("${type.collectionName} (Deleted Soon)", type.collectionType, AddLibraryRequest(), listOf(pathForEmby))
            goneSoonCollection = embyClient.listLibraries().firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        }

        // Clean up entire directory and rebuild from scratch - this can help with clearing orphaned data
        if (fileSystemProperties.fromScratch && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            Files.createDirectories(path)
        }

        createLinks(items, path, type)
    }

}