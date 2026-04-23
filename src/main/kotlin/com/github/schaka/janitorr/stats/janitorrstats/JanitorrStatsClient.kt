package com.github.schaka.janitorr.stats.janitorrstats

import com.github.schaka.janitorr.stats.janitorrstats.requests.JanitorrStatsPagedResponse
import com.github.schaka.janitorr.stats.janitorrstats.requests.JanitorrStatsPlayEvent
import feign.Param
import feign.RequestLine

/**
 * https://github.com/Schaka/janitorr-stats - history keyed by stable external IDs (IMDB/TMDB/TVDB).
 * We always request page=0, size=1 since we only need the most recent play event.
 */
interface JanitorrStatsClient {

    @RequestLine("GET /history/movies?imdbId={imdbId}&tmdbId={tmdbId}&page=0&size=100")
    fun getMovieHistory(@Param("imdbId") imdbId: String, @Param("tmdbId") tmdbId: Int): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/movies?imdbId={imdbId}&page=0&size=100")
    fun getMovieHistoryByImdb(@Param("imdbId") imdbId: String): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/movies?tmdbId={tmdbId}&page=0&size=100")
    fun getMovieHistoryByTmdb(@Param("tmdbId") tmdbId: Int): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/shows?tvdbId={tvdbId}&season={season}&page=0&size=100")
    fun getShowHistoryByTvdb(@Param("tvdbId") tvdbId: Int, @Param("season") season: Int): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/shows?tvdbId={tvdbId}&page=0&size=100")
    fun getShowHistoryByTvdb(@Param("tvdbId") tvdbId: Int): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/shows?imdbId={imdbId}&season={season}&page=0&size=100")
    fun getShowHistoryByImdb(@Param("imdbId") imdbId: String, @Param("season") season: Int): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/shows?imdbId={imdbId}&page=0&size=100")
    fun getShowHistoryByImdb(@Param("imdbId") imdbId: String): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/shows?tmdbId={tmdbId}&season={season}&page=0&size=100")
    fun getShowHistoryByTmdb(@Param("tmdbId") tmdbId: Int, @Param("season") season: Int): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

    @RequestLine("GET /history/shows?tmdbId={tmdbId}&page=0&size=100")
    fun getShowHistoryByTmdb(@Param("tmdbId") tmdbId: Int): JanitorrStatsPagedResponse<JanitorrStatsPlayEvent>

}
