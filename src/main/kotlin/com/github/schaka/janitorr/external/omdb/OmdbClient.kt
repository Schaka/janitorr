package com.github.schaka.janitorr.external.omdb

import feign.Param
import feign.RequestLine

interface OmdbClient {

    @RequestLine("GET /?apikey={apiKey}&i={imdbId}")
    fun getByImdbId(@Param("imdbId") imdbId: String, @Param("apiKey") apiKey: String): OmdbResponse

    @RequestLine("GET /?apikey={apiKey}&t={title}&y={year}")
    fun getByTitle(
        @Param("title") title: String,
        @Param("year") year: String?,
        @Param("apiKey") apiKey: String
    ): OmdbResponse
}
