package com.github.schaka.janitorr.jellyfin

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.jellyfin.library.*
import com.github.schaka.janitorr.jellyfin.library.LibraryType.MOVIES
import com.github.schaka.janitorr.jellyfin.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

@Service
@ConditionalOnProperty("clients.jellyfin.enabled", havingValue = "true")
class JellyfinRestService(

        val jellyfinClient: JellyfinClient,
        val jellyfinProperties: JellyfinProperties,
        val applicationProperties: ApplicationProperties,
        val fileSystemProperties: FileSystemProperties

        ) : JellyfinService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val seasonPattern = Regex("Season (?<season>\\d+)")
        private val filePattern = Regex("^.*\\.(mkv|mp4|avi|webm|mts|m2ts|ts|wmv|mpg|mpeg|mp2|m2v|m4v)\$")
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {
        val parentFolders = jellyfinClient.getAllItems()

        val jellyfinShows = parentFolders.Items.flatMap { parent ->
            jellyfinClient.getAllTvShows( parent.Id).Items.filter { it.IsSeries }.flatMap { show ->
                val seasons = jellyfinClient.getAllSeasons(show.Id).Items
                seasons.forEach { it.ProviderIds = show.ProviderIds } // we want IDs for the entire show to match, not season IDs (only available from tvdb)
                seasons
            }
        }

        for (show: LibraryItem in items) {
            jellyfinShows.firstOrNull{ tvShowMatches(show, it) }
                    ?.let {jellyfinContent ->
                        if (!applicationProperties.dryRun) {
                            jellyfinClient.deleteItemAndFiles(jellyfinContent.Id)
                        }
                        else {
                            log.info("Found {} {} on Jellyfin", jellyfinContent.SeriesName, jellyfinContent.Name)
                        }
                    }
        }
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
        val parentFolders = jellyfinClient.getAllItems()

        val jellyfinMovies = parentFolders.Items.flatMap {
            jellyfinClient.getAllMovies( it.Id).Items
        }

        for (movie: LibraryItem in items) {
            jellyfinMovies.firstOrNull{ mediaMatches(MOVIES, movie, it) }
                    ?.let {jellyfinContent ->
                        if (!applicationProperties.dryRun) {
                            jellyfinClient.deleteItemAndFiles(jellyfinContent.Id)
                        }
                        else {
                            log.info("Found {} on Jellyfin", jellyfinContent.Name)
                        }
                    }
        }
    }

    private fun tvShowMatches(item: LibraryItem, candidate: LibraryContent, matchSeason: Boolean = true): Boolean {
        val seasonMatches = candidate.Type == "Season" && candidate.Name.contains("Season") && item.season == seasonPattern.find(candidate.Name)?.groups?.get("season")?.value?.toInt()
        return mediaMatches(TV_SHOWS, item, candidate) && if (matchSeason) seasonMatches else true
    }

    private fun mediaMatches(type: LibraryType, item: LibraryItem, candidate: LibraryContent): Boolean {
        val imdbMatches = candidate.ProviderIds?.Imdb != null && (candidate.ProviderIds?.Imdb == item.imdbId)
        val tmdbMatches = candidate.ProviderIds?.Tmdb != null && mediaTypeMatches(type, candidate) && (candidate.ProviderIds?.Tmdb == item.tmdbId)
        val tvdbMatches = candidate.ProviderIds?.Tvdb != null && mediaTypeMatches(type, candidate) && (candidate.ProviderIds?.Tvdb == item.tvdbId)
        return imdbMatches || tmdbMatches || tvdbMatches
    }

    private fun mediaTypeMatches(type: LibraryType, content: LibraryContent): Boolean {
        return when (type) {
            MOVIES -> content.IsMovie
            TV_SHOWS -> content.IsSeries
        }
    }

    override fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access) {
            return
        }

        val result = jellyfinClient.listLibraries()
        val collectionFilter = type.collectionType.lowercase()
        val path = Path.of(fileSystemProperties.leavingSoonDir, type.folderName)

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we just create a media library that consists only
        var goneSoonCollection = result.firstOrNull { it.CollectionType == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        if (goneSoonCollection == null) {
            //Files.createDirectories(path)
            jellyfinClient.createLibrary("${type.collectionName} (Deleted Soon)", type.collectionType, AddLibraryRequest(), listOf(path.toUri().path))
            goneSoonCollection = jellyfinClient.listLibraries().firstOrNull { it.CollectionType == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        }

        items
                .forEach { it ->
                    try {

                        val rootPath = Path.of(it.rootFolderPath)
                        val itemPath = Path.of(it.fullPath)
                        val itemFolder = itemPath.subtract(rootPath).firstOrNull()


                        if (!applicationProperties.dryRun) {

                        }
                        else {
                            val fileOrFolder = Path.of(it.libraryPath).subtract(itemPath).firstOrNull() // contains filename and folder before it e.g. (Season 05) (ShowName-Episode01.mkv)
                            log.info("Creating folder {}", itemFolder?.pathString)

                            val targetFolder = Path.of(fileSystemProperties.leavingSoonDir).resolve(Path.of(type.folderName)).resolve(itemFolder)
                            //Files.createDirectories(targetFolder) // create folder that link will be placed in

                            // FIXME: Figure out if we're dealing with single episodes in a season when season folders are deactivated
                            // For now, just assume season folders are always activated

                            if (it.season != null && !filePattern.matches(fileOrFolder.toString())) {
                                // TV Shows
                                val sourceSeasonFolder = itemPath.resolve(fileOrFolder!!)
                                val targetSeasonFolder = targetFolder.resolve(fileOrFolder)
                                Files.createDirectories(targetSeasonFolder)
                                val files = sourceSeasonFolder.listDirectoryEntries().filter { dir ->  !dir.isDirectory() }
                                for (file in files) {
                                    val source = sourceSeasonFolder.resolve(file)
                                    val target = targetSeasonFolder.resolve(fileOrFolder)
                                    log.info("Creating link from {} to {}", source, target)
                                    //Files.createSymbolicLink(target, source)
                                }
                            }
                            else {
                                // Movies
                                val source = itemPath.resolve(fileOrFolder)
                                val target = targetFolder.resolve(fileOrFolder)
                                log.info("Creating link from {} to {}", source, target)
                                //Files.createSymbolicLink(target, source)
                            }
                        }
                    } catch (e: Exception) {
                        log.error("Couldn't find path {}", it.fullPath)
                    }
                }
    }

}