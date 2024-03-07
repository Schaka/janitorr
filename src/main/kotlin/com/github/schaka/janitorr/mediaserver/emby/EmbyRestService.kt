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
            // Windows paths may have a trailing trash - Windows Emby can't deal with that
            val pathForEmby = if (pathString.startsWith("/")) pathString.replaceFirst("/", "") else pathString
            embyClient.createLibrary("${type.collectionName} (Deleted Soon)", type.collectionType, AddLibraryRequest(), listOf(pathForEmby))
            goneSoonCollection = embyClient.listLibraries().firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        }

        // Clean up entire directory and rebuild from scratch - this can help with clearing orphaned data
        if (fileSystemProperties.fromScratch && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            Files.createDirectories(path)
        }

        // TODO: This entire block should probably go to the super class
        // It's not dependent on any client

        items.forEach {
            try {

                // FIXME: Figure out if we're dealing with single episodes in a season when season folders are deactivated in Sonarr
                // Idea: If we did have an item for every episode in a season, this might work
                // For now, just assume season folders are always activated
                val structure = pathStructure(it, path)

                if (type == LibraryType.TV_SHOWS && it.season != null && !isMediaFile(structure.sourceFile.toString())) {
                    // TV Shows
                    val sourceSeasonFolder = structure.sourceFile
                    val targetSeasonFolder = structure.targetFile
                    log.trace("Season folder - Source: {}, Target: {}", sourceSeasonFolder, targetSeasonFolder)

                    if (sourceSeasonFolder.exists()) {
                        log.trace("Creating season folder {}", targetSeasonFolder)
                        Files.createDirectories(targetSeasonFolder)

                        val files = sourceSeasonFolder.listDirectoryEntries().filter { f -> isMediaFile(f.toString()) }
                        for (file in files) {
                            val fileName = file.subtract(sourceSeasonFolder).firstOrNull()!!

                            val source = sourceSeasonFolder.resolve(fileName)
                            val target = targetSeasonFolder.resolve(fileName)
                            createSymLink(source, target, "episode")
                        }
                    } else {
                        log.info("Can't find original season folder - no links to create {}", sourceSeasonFolder)
                    }
                } else if (type == LibraryType.MOVIES) {
                    // Movies
                    val source = structure.sourceFile
                    log.trace("Movie folder - {}", structure)

                    if (source.exists()) {
                        val target = structure.targetFile
                        Files.createDirectories(structure.targetFolder)
                        createSymLink(source, target, "movie")
                    }
                    else {
                        log.info("Can't find original movie folder - no links to create {}", source)
                    }
                }
            } catch (e: Exception) {
                log.error("Couldn't find path {}", it.parentPath)
            }
        }
    }

}