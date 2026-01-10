package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * Handles library access for media server in a cacheable way.
 */
@Service
class MediaServerLibraryQueryService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        const val CACHE_NAME_MOVIES = "media-server-library-movies-cache"
        const val CACHE_NAME_TV = "media-server-library-tv-cache"
    }


    @Cacheable(CACHE_NAME_TV, key = "#bySeason")
    fun getTvLibrary(mediaServerClient: MediaServerClient, bySeason: Boolean = true): List<LibraryContent> {
        val parentFolders = mediaServerClient.getAllItems().Items.filter { it.Type != "ManualPlaylistsFolder" && it.Name != "Playlists" }

        var mediaServerShows = parentFolders.flatMap { parent ->
            mediaServerClient.getAllTvShows(parent.Id).Items.filter { it.IsSeries || it.Type.lowercase() == "series" }
        }

        // don't treat library season by season, if not necessary
        if (bySeason) {
            mediaServerShows = mediaServerShows.flatMap { show ->
                val seasons = mediaServerClient.getAllSeasons(show.Id).Items
                seasons.forEach { it.ProviderIds = show.ProviderIds } // we want metadata (IMDB, TMDB) IDs for the entire show to match, not season IDs (only available from TVDB)
                return@flatMap seasons
            }
        }

        return mediaServerShows
    }


    @Cacheable(CACHE_NAME_MOVIES)
    fun getMovieLibrary(mediaServerClient: MediaServerClient): List<LibraryContent> {
        val parentFolders = mediaServerClient.getAllItems().Items.filter { it.Type != "ManualPlaylistsFolder" && it.Name != "Playlists" }

        val mediaServerMovies = parentFolders.flatMap {
            mediaServerClient.getAllMovies(it.Id).Items
        }

        return mediaServerMovies
    }

}