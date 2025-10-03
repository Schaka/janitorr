package com.github.schaka.janitorr.external.tmdb

import feign.Param
import feign.RequestLine

interface TmdbClient {

    @RequestLine("GET /movie/{movieId}?api_key={apiKey}")
    fun getMovie(@Param("movieId") movieId: Int, @Param("apiKey") apiKey: String): TmdbMovieResponse

    @RequestLine("GET /tv/{tvId}?api_key={apiKey}")
    fun getTvShow(@Param("tvId") tvId: Int, @Param("apiKey") apiKey: String): TmdbTvResponse

    @RequestLine("GET /movie/{movieId}/external_ids?api_key={apiKey}")
    fun getMovieExternalIds(@Param("movieId") movieId: Int, @Param("apiKey") apiKey: String): TmdbExternalIds

    @RequestLine("GET /tv/{tvId}/external_ids?api_key={apiKey}")
    fun getTvExternalIds(@Param("tvId") tvId: Int, @Param("apiKey") apiKey: String): TmdbExternalIds

    @RequestLine("GET /trending/{mediaType}/{timeWindow}?api_key={apiKey}")
    fun getTrending(
        @Param("mediaType") mediaType: String, 
        @Param("timeWindow") timeWindow: String,
        @Param("apiKey") apiKey: String
    ): TmdbTrendingResponse

    @RequestLine("GET /find/{externalId}?api_key={apiKey}&external_source=imdb_id")
    fun findByImdbId(@Param("externalId") externalId: String, @Param("apiKey") apiKey: String): TmdbFindResponse
}

data class TmdbFindResponse(
    val movie_results: List<TmdbMovieResponse>,
    val tv_results: List<TmdbTvResponse>
)
