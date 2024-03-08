package com.github.schaka.janitorr.mediaserver.jellyfin

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.jellyfin.library.AddLibraryRequest
import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryType
import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path

@Service
@ConditionalOnProperty("clients.jellyfin.enabled", havingValue = "true", matchIfMissing = false)
class JellyfinRestService(

    val jellyfinClient: JellyfinClient,
    val jellyfinUserClient: JellyfinUserClient,
    val jellyfinProperties: JellyfinProperties,
    val applicationProperties: ApplicationProperties,
    val fileSystemProperties: FileSystemProperties

) : MediaServerService() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {
        val parentFolders = jellyfinClient.getAllItems()

        val jellyfinShows = parentFolders.Items.flatMap { parent ->
            jellyfinClient.getAllTvShows(parent.Id).Items.filter { it.IsSeries }.flatMap { show ->
                val seasons = jellyfinClient.getAllSeasons(show.Id).Items
                seasons.forEach { it.ProviderIds = show.ProviderIds } // we want IDs for the entire show to match, not season IDs (only available from tvdb)
                seasons
            }
        }

        for (show: LibraryItem in items) {
            jellyfinShows.firstOrNull { tvShowMatches(show, it) }
                ?.let { jellyfinContent ->
                    if (!applicationProperties.dryRun) {
                        jellyfinUserClient.deleteItemAndFiles(jellyfinContent.Id)
                        log.info("Deleting {} {} from Jellyfin", jellyfinContent.SeriesName, jellyfinContent.Name)
                    } else {
                        log.info("Found {} {} on Jellyfin", jellyfinContent.SeriesName, jellyfinContent.Name)
                    }
                }
        }

        // TODO: Remove TV shows if all seasons gone
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
        val parentFolders = jellyfinClient.getAllItems()

        val jellyfinMovies = parentFolders.Items.flatMap {
            jellyfinClient.getAllMovies(it.Id).Items
        }

        for (movie: LibraryItem in items) {
            jellyfinMovies.firstOrNull { mediaMatches(MOVIES, movie, it) }
                ?.let { jellyfinContent ->
                    if (!applicationProperties.dryRun) {
                        jellyfinUserClient.deleteItemAndFiles(jellyfinContent.Id)
                        log.info("Deleting {} from Jellyfin", jellyfinContent.Name)
                    } else {
                        log.info("Found {} on Jellyfin", jellyfinContent.Name)
                    }
                }
        }
    }

    private fun tvShowMatches(item: LibraryItem, candidate: LibraryContent, matchSeason: Boolean = true): Boolean {
        val seasonMatches = candidate.Type == "Season"
                && candidate.Name.contains("Season")
                && item.season == seasonPattern.find(candidate.Name)?.groups?.get("season")?.value?.toInt()

        return mediaMatches(TV_SHOWS, item, candidate) && if (matchSeason) seasonMatches else true
    }

    private fun mediaMatches(type: LibraryType, item: LibraryItem, candidate: LibraryContent): Boolean {
        // Check if the media type is the same before checking anything else
        if (!mediaTypeMatches(type, candidate)) {
            return false
        }

        val tmdbId = parseMetadataId(candidate.ProviderIds?.Tmdb)
        val tvdbId = parseMetadataId(candidate.ProviderIds?.Tvdb)

        val imdbMatches = candidate.ProviderIds?.Imdb != null && candidate.ProviderIds?.Imdb == item.imdbId
        val tmdbMatches = candidate.ProviderIds?.Tmdb != null && tmdbId == item.tmdbId
        val tvdbMatches = candidate.ProviderIds?.Tvdb != null && tvdbId == item.tvdbId
        return imdbMatches || tmdbMatches || tvdbMatches
    }

    private fun mediaTypeMatches(type: LibraryType, content: LibraryContent): Boolean {
        return when (type) {
            MOVIES -> content.IsMovie
            TV_SHOWS -> content.IsSeries
        }
    }

    override fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access || fileSystemProperties.leavingSoonDir == null) {
            return
        }

        val result = jellyfinClient.listLibraries()
        val collectionFilter = type.collectionType.lowercase()
        // subdirectory (i.e. /leaving-soon/tv
        val path = Path.of(fileSystemProperties.leavingSoonDir, type.folderName)

        // Clean up library - consider also deleting the collection in Jellyfin
        if (items.isEmpty()) {
            FileSystemUtils.deleteRecursively(path)
            return
        }

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we just create a media library that consists only
        var goneSoonCollection = result.firstOrNull { it.CollectionType == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        if (goneSoonCollection == null) {
            Files.createDirectories(path)
            val pathString = path.toUri().path
            // Windows paths may have a trailing trash - Windows Jellyfin can't deal with that, this is a bit hacky but makes development easier
            val pathforJellyfin = if (pathString.startsWith("/C:")) pathString.replaceFirst("/", "") else pathString
            jellyfinClient.createLibrary("${type.collectionName} (Deleted Soon)", type.collectionType, AddLibraryRequest(), listOf(pathforJellyfin))
            goneSoonCollection = jellyfinClient.listLibraries().firstOrNull { it.CollectionType == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        }

        // Clean up entire directory and rebuild from scratch - this can help with clearing orphaned data
        if (fileSystemProperties.fromScratch && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            Files.createDirectories(path)
        }

        createLinks(items, path, type)
    }

}