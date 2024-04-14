package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.library.AddLibraryRequest
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path

/**
 * Keep 2 layers of abstract classes. The time is 100% going to come when Emby will split off from Jellyfin or the other way around.
 * By the point, it'll be much easier to refactor this.
 */
abstract class AbstractMediaServerRestService(

        val serviceName: String,
        val mediaServerClient: MediaServerClient,
        val mediaServerUserClient: MediaServerUserClient,
        val mediaServerProperties: MediaServerProperties,
        val applicationProperties: ApplicationProperties,
        val fileSystemProperties: FileSystemProperties

) : MediaServerService() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    /**
     * Populates the library items with Jellyfin/Emby IDs if available.
     * This can be used for easier matching by other components like Jellyseerr and Jellystat, which use the same IDs.
     */
    override fun populateMediaServerIds(items: List<LibraryItem>, type: LibraryType) {
        when (type) {
            TV_SHOWS -> populateTvShowIds(items)
            MOVIES -> populateMovieIds(items)
        }
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {

        if (!mediaServerProperties.delete) {
            log.info("Deletion from $serviceName disabled")
            return
        }

        val mediaServerShows = getTvLibrary()

        for (show: LibraryItem in items) {
            mediaServerShows.firstOrNull { tvShowMatches(show, it) }
                    ?.let { mediaServerContent ->
                        if (!applicationProperties.dryRun) {
                            try {
                                mediaServerUserClient.deleteItemAndFiles(mediaServerContent.Id)
                                log.info("Deleting {} {} from $serviceName", mediaServerContent.SeriesName, mediaServerContent.Name)
                            } catch (e: Exception) {
                                log.error("Deleting {} {} from $serviceName", mediaServerContent.SeriesName, mediaServerContent.Name, e)
                            }
                        } else {
                            log.info("Found {} {} on $serviceName", mediaServerContent.SeriesName, mediaServerContent.Name)
                        }
                    }
        }

        // TODO: Remove TV shows if all seasons gone
    }

    private fun populateTvShowIds(items: List<LibraryItem>) {
        val mediaServerShows = getTvLibrary()
        for (show: LibraryItem in items) {
            mediaServerShows.firstOrNull { tvShowMatches(show, it) }
                    ?.let { mediaServerContent ->
                        show.mediaServerId = mediaServerContent.Id
                    }
        }
    }

    private fun getTvLibrary(): List<LibraryContent> {
        val parentFolders = mediaServerClient.getAllItems()

        val mediaServerShows = parentFolders.Items.flatMap { parent ->
            mediaServerClient.getAllTvShows(parent.Id).Items.filter { it.IsSeries || it.Type == "Series" }.flatMap { show ->
                val seasons = mediaServerClient.getAllSeasons(show.Id).Items
                seasons.forEach { it.ProviderIds = show.ProviderIds } // we want IDs for the entire show to match, not season IDs (only available from tvdb)
                seasons
            }
        }

        return mediaServerShows
    }

    private fun populateMovieIds(items: List<LibraryItem>) {
        val mediaServerMovies = getMovieLibrary()

        for (movie: LibraryItem in items) {
            mediaServerMovies.firstOrNull { mediaMatches(MOVIES, movie, it) }
                    ?.let { mediaServerContent ->
                        movie.mediaServerId = mediaServerContent.Id
                    }
        }
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
        val mediaServerMovies = getMovieLibrary()

        for (movie: LibraryItem in items) {
            mediaServerMovies.firstOrNull { mediaMatches(MOVIES, movie, it) }
                    ?.let { mediaServerContent ->
                        if (!applicationProperties.dryRun) {
                            try {
                                mediaServerUserClient.deleteItemAndFiles(mediaServerContent.Id)
                                log.info("Deleting {} from $serviceName", mediaServerContent.Name)
                            } catch (e: Exception) {
                                log.error("Deleting from $serviceName failed", e)
                            }
                        } else {
                            log.info("Found {} on $serviceName", mediaServerContent.Name)
                        }
                    }
        }
    }

    private fun getMovieLibrary(): List<LibraryContent> {
        val parentFolders = mediaServerClient.getAllItems()

        val mediaServerMovies = parentFolders.Items.flatMap {
            mediaServerClient.getAllMovies(it.Id).Items
        }

        return mediaServerMovies
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
            MOVIES -> content.IsMovie || content.Type == "Movie"
            TV_SHOWS -> content.IsSeries || content.Type == "Season" || content.Type == "Series"
        }
    }

    override fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access || fileSystemProperties.leavingSoonDir == null) {
            return
        }

        val result = mediaServerClient.listLibraries()
        val collectionFilter = type.collectionType.lowercase()
        // subdirectory (i.e. /leaving-soon/tv
        val path = Path.of(fileSystemProperties.leavingSoonDir, type.folderName)

        // Clean up library - consider also deleting the collection in Jellyfin/Emby
        if (items.isEmpty()) {
            FileSystemUtils.deleteRecursively(path)
            return
        }

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we just create a media library that consists only
        var goneSoonCollection = result.firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        if (goneSoonCollection == null) {
            Files.createDirectories(path)
            val pathString = path.toUri().path
            // Windows paths may have a trailing trash - Windows Jellyfin/Emby can't deal with that, this is a bit hacky but makes development easier
            val pathForMediaServer = if (pathString.startsWith("/C:")) pathString.replaceFirst("/", "") else pathString
            mediaServerClient.createLibrary("${type.collectionName} (Deleted Soon)", type.collectionType, AddLibraryRequest(), listOf(pathForMediaServer))
            goneSoonCollection = mediaServerClient.listLibraries().firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        }

        // Clean up entire directory and rebuild from scratch - this can help with clearing orphaned data
        if (fileSystemProperties.fromScratch && !onlyAddLinks) {
            FileSystemUtils.deleteRecursively(path)
            Files.createDirectories(path)
        }

        createLinks(items, path, type)
    }

}