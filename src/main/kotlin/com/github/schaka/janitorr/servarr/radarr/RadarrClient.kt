package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.servarr.radarr.movie.MoviePayload
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import feign.Param
import feign.RequestLine

interface RadarrClient {

    @RequestLine("GET /history/movie?movieId={movieId}")
    fun getHistory(@Param("movieId") movieId: Int): List<HistoryResponse>

    @RequestLine("GET /movie")
    fun getAllMovies(): List<MoviePayload>

    @RequestLine("GET /movie/{id}")
    fun getMovie(@Param("id") id: Int): MoviePayload

    @RequestLine("PUT /movie/{id}")
    fun updateMovie(@Param("id") id: Int, payload: MoviePayload)

    @RequestLine("DELETE /movie/{id}?deleteFiles={deleteFiles}")
    fun deleteMovie(@Param("id") id: Int, @Param("deleteFiles") deleteFiles: Boolean = true)

    @RequestLine("GET /qualityprofile")
    fun getAllQualityProfiles(): List<QualityProfile>
}