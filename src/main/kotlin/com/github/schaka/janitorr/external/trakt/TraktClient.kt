package com.github.schaka.janitorr.external.trakt

import feign.Param
import feign.RequestLine

interface TraktClient {

    @RequestLine("GET /movies/{id}/stats")
    fun getMovieStats(@Param("id") id: String): TraktStatsResponse

    @RequestLine("GET /shows/{id}/stats")
    fun getShowStats(@Param("id") id: String): TraktStatsResponse

    @RequestLine("GET /movies/trending")
    fun getTrendingMovies(): List<TraktTrendingItem>

    @RequestLine("GET /shows/trending")
    fun getTrendingShows(): List<TraktTrendingItem>

    @RequestLine("GET /search/imdb/{id}")
    fun searchByImdbId(@Param("id") imdbId: String): List<TraktSearchResult>

    @RequestLine("GET /search/tmdb/{id}?type={type}")
    fun searchByTmdbId(@Param("id") tmdbId: Int, @Param("type") type: String): List<TraktSearchResult>
}

data class TraktSearchResult(
    val type: String,
    val score: Double,
    val movie: TraktMovieResponse?,
    val show: TraktShowResponse?
)
