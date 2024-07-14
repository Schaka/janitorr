package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.jellystat.JellystatProperties
import com.github.schaka.janitorr.mediaserver.library.*
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
    override fun populateMediaServerIds(items: List<LibraryItem>, type: LibraryType, config: JellystatProperties) {
        when (type) {
            TV_SHOWS -> populateTvShowIds(items, !config.wholeTvShow)
            MOVIES -> populateMovieIds(items)
        }
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {

        if (!mediaServerProperties.delete) {
            log.info("Deletion from $serviceName disabled")
            return
        }

        // if we're treating shows as a whole, getTvLibrary will return all TV shows, not seasons
        // we also filter our library items, so we only need to match and delete each show once
        val mediaServerShows = getTvLibrary()
        val showsForDeletion = if (applicationProperties.wholeTvShow) items.distinctBy { it.id } else items

        for (show: LibraryItem in showsForDeletion) {
            mediaServerShows
                .firstOrNull { tvShowMatches(show, it) }
                    // if we find any matches for TV show or season (depending on settings) delete that match
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

        // TODO: Remove TV shows if all seasons gone - only if wholeShow is turned off
    }

    private fun populateTvShowIds(items: List<LibraryItem>, bySeason: Boolean = true) {

        val useSeason = !applicationProperties.wholeTvShow && bySeason

        val mediaServerShows = getTvLibrary(useSeason)
        for (show: LibraryItem in items) {
            mediaServerShows.firstOrNull { tvShowMatches(show, it, useSeason) }
                    ?.let { mediaServerContent ->
                        show.mediaServerId = mediaServerContent.Id
                    }
        }
    }

    private fun getTvLibrary(bySeason: Boolean = true): List<LibraryContent> {
        val parentFolders = mediaServerClient.getAllItems()

        var mediaServerShows = parentFolders.Items.flatMap { parent ->
            mediaServerClient.getAllTvShows(parent.Id).Items.filter { it.IsSeries || it.Type == "Series" }
        }

        // don't treat library season by season, if not necessary
        if (bySeason) {
            mediaServerShows = mediaServerShows.flatMap { show ->
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
        val tmdbMatches = tmdbId != null && item.tmdbId != null && tmdbId == item.tmdbId
        val tvdbMatches = tvdbId != null && item.tvdbId != null && tvdbId == item.tvdbId
        return imdbMatches || tmdbMatches || tvdbMatches
    }

    private fun mediaTypeMatches(type: LibraryType, content: LibraryContent): Boolean {
        return when (type) {
            MOVIES -> content.IsMovie || content.Type == "Movie"
            TV_SHOWS -> content.IsSeries || content.Type == "Season" || content.Type == "Series"
        }
    }

    override fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access || fileSystemProperties.leavingSoonDir == null) {
            return
        }

        val result = mediaServerClient.listLibraries()
        val collectionFilter = libraryType.collectionType.lowercase()
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
        var leavingSoonCollection = result.firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == libraryName }
        if (leavingSoonCollection == null) {
            // Windows paths may have a trailing trash - Windows Jellyfin/Emby can't deal with that, this is a bit hacky but makes development easier
            val pathForMediaServer = if (pathString.startsWith("/C:")) pathString.replaceFirst("/", "") else pathString
            mediaServerClient.createLibrary(libraryName, libraryType.collectionType, AddLibraryRequest(), listOf(pathForMediaServer))
            leavingSoonCollection = mediaServerClient.listLibraries().firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == libraryName }
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