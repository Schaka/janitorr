package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.mediaserver.library.VirtualFolderResponse
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.bazarr.BazarrPayload
import com.github.schaka.janitorr.servarr.bazarr.BazarrService
import org.slf4j.LoggerFactory
import org.springframework.util.FileSystemUtils
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Keep 2 layers of abstract classes. The time is 100% going to come when Emby will split off from Jellyfin or the other way around.
 * By the point, it'll be much easier to refactor this.
 */
abstract class BaseMediaServerService(

    val serviceName: String,
    val mediaServerClient: MediaServerClient,
    val mediaServerUserClient: MediaServerUserClient,
    val bazarrService: BazarrService,
    val mediaServerLibraryQueryService: MediaServerLibraryQueryService,
    val mediaServerProperties: MediaServerProperties,
    val applicationProperties: ApplicationProperties,
    val fileSystemProperties: FileSystemProperties

) : AbstractMediaServerService() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val windowsRegex = Regex("/\\w:.*")
    }

    /**
     * Populates the library items with Jellyfin/Emby IDs if available.
     * This can be used for easier matching by other components like Jellyseerr and Jellystat, which use the same IDs.
     * Population happens per movie, TV show or season - but never per episode.
     */
    override fun populateMediaServerIds(items: List<LibraryItem>, type: LibraryType, bySeason: Boolean) {

        if (mediaServerProperties.excludeFavorited) {
            val mappings = when (type) {
                TV_SHOWS -> getMediaServerIdsForTvShowIds(items, bySeason)
                MOVIES -> getMediaServerIdsForMovieIds(items)
            }

            items.forEach { item -> item.mediaServerIds.addAll(mappings.getOrDefault(item.id, listOf())) }
        }
    }

    override fun getMediaServerIdsForLibrary(items: List<LibraryItem>, type: LibraryType, bySeason: Boolean): Map<Int, List<String>> {
        return when (type) {
            TV_SHOWS -> getMediaServerIdsForTvShowIds(items, bySeason)
            MOVIES -> getMediaServerIdsForMovieIds(items)
        }
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {

        if (!mediaServerProperties.delete) {
            log.info("Deletion for $serviceName disabled - not deleting any orphaned files")
            return
        }

        // if we're treating shows as a whole, getTvLibrary will return all TV shows, not seasons
        // we also filter our library items, so we only need to match and delete each show once
        val mediaServerShows = getTvLibrary()
        val showsForDeletion = if (applicationProperties.wholeTvShow) items.distinctBy { it.id } else items

        for (show: LibraryItem in showsForDeletion) {
            mediaServerShows
                .filter { tvShowMatches(show, it) }
                // if we find any matches for TV show or season (depending on settings) delete that match
                .forEach { mediaServerContent ->
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

    private fun getMediaServerIdsForTvShowIds(items: List<LibraryItem>, bySeason: Boolean = true): Map<Int, List<String>> {

        // Do we need to aggregate by season or give every episode/season the entire TV show ID?
        val useSeason = !applicationProperties.wholeTvShow && bySeason

        val mediaServerShows = getTvLibrary(useSeason)

        // it's not worth caching the showId => mediaServerIds lookup directly, it gets called too rarely, and we need to iterate the entire library to fill the cache manually anyway
        return items
            .groupBy { show -> show.id }
            .mapValues { (_, showsInGroup) ->
                showsInGroup.flatMap { show ->
                    mediaServerShows
                        .filter { tvShowMatches(show, it) }
                        .map { it.Id }
                }
            }
    }

    private fun getTvLibrary(bySeason: Boolean = true): List<LibraryContent> {
        return mediaServerLibraryQueryService.getTvLibrary(mediaServerClient, bySeason)
    }

    private fun getMediaServerIdsForMovieIds(items: List<LibraryItem>): Map<Int, List<String>> {
        val mediaServerMovies = getMovieLibrary()

        // it's not worth caching the movieId => mediaServerIds lookup directly, it gets called too rarely, and we need to iterate the entire library to fill the cache manually anyway
        return items
            .groupBy { movie -> movie.id }
            .mapValues { (_, moviesInGroup) ->
                moviesInGroup.flatMap { movie ->
                    mediaServerMovies
                        .filter { mediaMatches(MOVIES, movie, it) }
                        .map { it.Id }
                }
            }
    }

    override fun cleanupMovies(items: List<LibraryItem>) {

        if (!mediaServerProperties.delete) {
            log.info("Deletion for $serviceName disabled - not deleting any orphaned files")
            return
        }

        val mediaServerMovies = getMovieLibrary()

        for (movie: LibraryItem in items) {
            mediaServerMovies
                .filter { mediaMatches(MOVIES, movie, it) }
                .forEach { mediaServerContent ->
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
        return mediaServerLibraryQueryService.getMovieLibrary(mediaServerClient)
    }

    // TODO: the right way is getting the server's localization settings and checking all of Jellyfin's location for the key NameSeasonNumber
    // https://api.jellyfin.org/#tag/Configuration/operation/GetConfiguration (UICulture)
    // https://github.com/jellyfin/jellyfin/blob/master/Emby.Server.Implementations/Localization/Core/ca.json
    private fun tvShowMatches(item: LibraryItem, candidate: LibraryContent, matchSeason: Boolean = true): Boolean {
        val seasonMatchesTitle = candidate.Type.lowercase() == "season"
                // && candidate.Name.contains("Season")
                // && item.season == seasonPattern.find(candidate.Name)?.groups?.get("season")?.value?.toInt()
                && item.season == seasonPatternLanguageAgnostic.find(candidate.Name)?.groups?.get("season")?.value?.toInt()

        val seasonMatchesIndex = candidate.Type.lowercase() == "season" && item.season == candidate.IndexNumber

        return mediaMatches(TV_SHOWS, item, candidate) && if (matchSeason) seasonMatchesTitle || seasonMatchesIndex else true
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
            MOVIES -> content.IsMovie || content.Type.lowercase() == "movie" || content.Type.lowercase() == "movies"
            TV_SHOWS -> content.IsSeries || content.Type.lowercase() == "season" || content.Type.lowercase() == "series"
        }
    }

    protected abstract fun listLibraries(): List<VirtualFolderResponse>

    protected abstract fun createLibrary(libraryName: String, libraryType: LibraryType, pathForMediaServer: String): VirtualFolderResponse

    protected abstract fun addPathToLibrary(leavingSoonCollection: VirtualFolderResponse, pathForMediaServer: String)

    override fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {

        // Only do this, if we can get access to the file system to create a link structure
        if (!fileSystemProperties.access || !mediaServerProperties.leavingSoonType.isAllowedForLibraryType(libraryType) ) {
            return
        }

        val result = listLibraries()
        val collectionFilter = libraryType.collectionType.lowercase()
        // subdirectory (i.e. /leaving-soon/tv/media, /leaving-soon/movies/tag-based
        val path = Path(fileSystemProperties.leavingSoonDir, libraryType.folderName, cleanupType.folderName)
        val mediaServerPath = Path(fileSystemProperties.mediaServerLeavingSoonDir ?: fileSystemProperties.leavingSoonDir, libraryType.folderName, cleanupType.folderName)
        val pathString = mediaServerPath.toUri().path.removeSuffix("/")
        // Windows paths may have a trailing trash - Windows Jellyfin/Emby can't deal with that, this is a bit hacky but makes development easier
        val pathForMediaServer = if (windowsRegex.matches(pathString)) pathString.replaceFirst("/", "") else pathString

        // Clean up library - consider also deleting the collection in Jellyfin/Emby
        if (items.isEmpty()) {
            if (!onlyAddLinks) {
                FileSystemUtils.deleteRecursively(path)
                Files.createDirectories(path)
                createEmptyFile(path)
            }
            return
        }

        Files.createDirectories(path)
        val libraryName = libraryType.collectionName(mediaServerProperties)

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we just create a media library that consists only
        var leavingSoonCollection = result.firstOrNull { it.CollectionType?.lowercase() == collectionFilter && it.Name == libraryName }
        if (leavingSoonCollection == null) {
            leavingSoonCollection = createLibrary(libraryName, libraryType, pathForMediaServer)
        }

        log.trace("Leaving Soon Collection Created/Found: {}", leavingSoonCollection)

        // the collection has been found, but maybe our cleanupType specific path hasn't been added to it yet
        if (!leavingSoonCollection.Locations.contains(pathForMediaServer)) {
            addPathToLibrary(leavingSoonCollection, pathForMediaServer)
        }

        // Clean up entire directory and rebuild from scratch - this can help with clearing orphaned data
        if (fileSystemProperties.fromScratch && !onlyAddLinks) {
            cleanupPath(fileSystemProperties.leavingSoonDir, libraryType, cleanupType)
        }

        populateExtraFiles(libraryType, items)
        createLinks(items, path, libraryType)
        createEmptyFile(path)
    }

    override fun getAllFavoritedItems(): List<LibraryContent> {
        if (!mediaServerProperties.excludeFavorited || !mediaServerProperties.enabled) {
            return emptyList()
        }

        val users = mediaServerClient.listUsers()

        return users.flatMap { user ->
                try {
                    mediaServerClient.getUserFavorites(user.Id).Items
                } catch (e: Exception) {
                    log.warn("Failed to fetch favorites for user {}", user.Name, e)
                    emptyList()
                }
            }.distinctBy { it.Id }
    }

    override fun filterOutFavorites(items: List<LibraryItem>, libraryType: LibraryType): List<LibraryItem> {
        val favoritedItems = getAllFavoritedItems()

        if (favoritedItems.isEmpty()) {
            return items
        }

        return items.filterNot { item ->
            val isFavorited = favoritedItems.any { favorite -> mediaMatches(libraryType, item, favorite)}
            if (isFavorited) {
                log.debug("Excluding favorited item from deletion: {} (IMDB: {}, TMDB: {}, TVDB: {})",
                    item.libraryPath, item.imdbId, item.tmdbId, item.tvdbId)
            }
            return@filterNot isFavorited
        }
    }

    private fun populateExtraFiles(type: LibraryType, items: List<LibraryItem>) {
        for (item in items) {
            val extraFiles = when (type) {
                MOVIES -> gracefulRequest(item.id, bazarrService::getSubtitlesForMovies)
                TV_SHOWS -> gracefulRequest(item.id, bazarrService::getSubtitlesForTv).filter { it.season == item.season }
            }.flatMap { it.subtitles }.mapNotNull { it.path }

            item.extraFiles += extraFiles
            log.trace("Adding extra files to {} for *arr id {} (season {}): {}", type, item.id, item.season, extraFiles)
        }
    }

    private fun gracefulRequest(id: Int, httpApiCall: (id: Int) -> List<BazarrPayload>): List<BazarrPayload> {
        try {
            return httpApiCall(id)
        } catch (e: Exception) {
            log.debug("Failed to request data from Bazarr", e)
        }
        return emptyList()
    }

    /**
     * Jellyfin/Emby require a file inside a library or they won't scan updates to media if all media was deleted.
     * https://github.com/jellyfin/jellyfin/issues/11913
     */
    private fun createEmptyFile(path: Path) {
        val fileName = "empty-file.media"
        val file = path.resolve(fileName)
        try {
            Files.createFile(file)
        } catch (e: FileAlreadyExistsException) {
            log.trace("File already exists: {}", file, e)
        } catch (e: IOException) {
            log.warn("Could not create empty file {}", fileName, e)
        }
    }

}