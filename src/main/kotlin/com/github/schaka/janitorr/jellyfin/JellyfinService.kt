package com.github.schaka.janitorr.jellyfin

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.jellyfin.library.*
import com.github.schaka.janitorr.jellyfin.library.LibraryType.MOVIES
import com.github.schaka.janitorr.jellyfin.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class JellyfinService(

        val jellyfinClient: JellyfinClient,
        val jellyfinProperties: JellyfinProperties,
        val applicationProperties: ApplicationProperties

        ) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val seasonPattern = Regex("Season (?<season>\\d+)")
    }

    fun cleanupTvShows(items: List<LibraryItem>) {
        val parentFolders = jellyfinClient.getAllItems()

        val jellyfinShows = parentFolders.Items.flatMap {
            jellyfinClient.getAllTvShows( it.Id).Items.flatMap { show ->
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

    fun cleanupMovies(items: List<LibraryItem>) {
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

    fun updateGoneSoonB(type: LibraryType, items: List<LibraryItem>) {

        val result = jellyfinClient.listLibraries()
        val collectionFilter = type.collectionType?.lowercase()

        // Collections are created via the Collection API, but it just puts them into a BoxSet library called collections
        // They're also a lot harder (imho) to manage - so we abuse this structure and create our own BoxSet
        var goneSoonCollection = result.firstOrNull { it.CollectionType == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }

        if (goneSoonCollection == null) {
            jellyfinClient.createLibrary("${type.collectionName} (Deleted Soon)", type?.collectionType ?: "INVALID", AddLibraryRequest())
            goneSoonCollection = jellyfinClient.listLibraries().firstOrNull { it.CollectionType == collectionFilter && it.Name == "${type.collectionName} (Deleted Soon)" }
        }

        val today = LocalDateTime.now()
        items
                .filter { it.date.plusMonths(3) < today /*&& it.date.plusMonths(4) > today */}
                .filter{ goneSoonCollection?.Locations?.contains(it.fullPath) == false }
                //.distinctBy { it.jellyfinPath }
                .forEach {
                    try {
                        // TODO: don't use BoxSet, instead create collection, find items in existing jellyfin library, add to collection - at least for TV shows?
                        //var collectionId = jellyfinClient.createCollection("${type.collectionName} (Deleted Soon)", goneSoonCollection!!.ItemId).Id
                        jellyfinClient.addPathToLibrary(AddPathRequest("${type.collectionName} (Deleted Soon)", it.fullPath, PathInfo(it.fullPath)))
                    } catch (e: Exception) {
                        log.error("Couldn't find path {}", it.fullPath)
                    }
                }
    }

    fun updateGoneSoonC(type: LibraryType, items: List<LibraryItem>) {

        val result = jellyfinClient.listLibraries()
        var parentCollection = result.firstOrNull { it.CollectionType == "boxsets" && it.Name == "Leaving Soon - watch before they're gone" }
        if (parentCollection == null) {
            jellyfinClient.createLibrary("Leaving Soon - watch before they're gone", "boxsets", AddLibraryRequest(), listOf("/config/data/data/collections"))
            parentCollection = jellyfinClient.listLibraries().firstOrNull { it.CollectionType == "boxsets" && it.Name == "Leaving Soon - watch before they're gone" }
        }

        jellyfinClient.createCollection("${type.collectionName} (Deleted Soon)", parentCollection?.ItemId)
    }

    fun updateGoneSoonE(type: LibraryType, items: List<LibraryItem>) {

        val result = jellyfinClient.listLibraries()
        var parentCollections = result.filter { it.CollectionType == type.collectionType?.lowercase() }
        for (parentCollection in parentCollections) {
            var exists = result.firstOrNull { it.CollectionType == "boxsets" && it.Name == "Leaving Soon - watch before they're gone" } != null
            if (!exists) {
                jellyfinClient.createCollection("Leaving Soon - watch before they're gone", parentCollection.ItemId)
            }
        }
    }


    fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>) {
        // DO NOTHING
        // WAIT UNTIL https://forum.jellyfin.org/t-api-virtualfolders-missing-itemid is solved?
    }

}