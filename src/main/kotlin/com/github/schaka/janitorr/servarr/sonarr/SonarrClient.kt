package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import com.github.schaka.janitorr.servarr.sonarr.episodes.EpisodeFile
import com.github.schaka.janitorr.servarr.sonarr.episodes.EpisodeResponse
import com.github.schaka.janitorr.servarr.sonarr.series.SeriesPayload
import feign.Param
import feign.RequestLine

interface SonarrClient {

    @RequestLine("GET /history/series?seriesId={seriesId}&seasonNumber={season}")
    fun getHistory(@Param("seriesId") seriesId: Int, @Param("season") season: Int): List<HistoryResponse>

    @RequestLine("GET /series/{id}")
    fun getSeries(@Param("id") id: Int): SeriesPayload

    @RequestLine("PUT /series/{id}")
    fun updateSeries(@Param("id") id: Int, payload: SeriesPayload)

    @RequestLine("GET /series")
    fun getAllSeries(): List<SeriesPayload>

    @RequestLine("GET /tag")
    fun getAllTags(): List<Tag>

    @RequestLine("DELETE /series/{id}?deleteFiles={files}&addImportListExclusion={blacklist}")
    fun deleteSeries(@Param("id") id: Int, @Param("files") files: Boolean = false, @Param("blacklist") blacklist: Boolean = false)

    @RequestLine("GET /episode?seriesId={seriesId}&seasonNumber={seasonNumber}&episodeIds={ids}")
    fun getAllEpisodes(@Param("seriesId") seriesId: Int, @Param("seasonNumber") seasonNumber: Int, @Param("ids") ids: List<Int>? = null): List<EpisodeResponse>

    @RequestLine("GET /episodefile/{id}")
    fun getEpisodeFile(@Param("id") episodeFileId: Int): EpisodeResponse

    @RequestLine("DELETE /episodefile/{id}")
    fun deleteEpisodeFile(@Param("id") episodeFileId: Int)

    @RequestLine("GET /qualityprofile")
    fun getAllQualityProfiles(): List<QualityProfile>


}