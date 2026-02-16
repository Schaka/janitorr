package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.servarr.data_structures.RadarrImportListExclusion
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import com.github.schaka.janitorr.servarr.radarr.movie.MovieFile
import com.github.schaka.janitorr.servarr.radarr.movie.MoviePayload
import feign.Param
import feign.RequestLine

interface RadarrClient {

    @RequestLine("GET /history/movie?movieId={movieId}")
    fun getHistory(@Param("movieId") movieId: Int): List<HistoryResponse>

    @RequestLine("GET /movie")
    fun getAllMovies(): List<MoviePayload>

    @RequestLine("GET /moviefile?movieId={id}")
    fun getMovieFiles(@Param("id") id: Int): List<MovieFile>

    @RequestLine("GET /tag")
    fun getAllTags(): List<Tag>

    @RequestLine("GET /movie/{id}")
    fun getMovie(@Param("id") id: Int): MoviePayload

    @RequestLine("PUT /movie/{id}")
    fun updateMovie(@Param("id") id: Int, payload: MoviePayload)

    @RequestLine("DELETE /movie/{id}?deleteFiles={deleteFiles}&addImportExclusion={addImportExclusion}")
    fun deleteMovie(@Param("id") id: Int, @Param("deleteFiles") deleteFiles: Boolean = true, @Param("addImportExclusion") addImportExclusion: Boolean = false)

    @RequestLine("DELETE /moviefile/{id}")
    fun deleteMovieFile(@Param("id") id: Int)

    @RequestLine("GET /qualityprofile")
    fun getAllQualityProfiles(): List<QualityProfile>

    @RequestLine("POST /exclusions")
    fun addToImportExclusion(exclusion: RadarrImportListExclusion)
}