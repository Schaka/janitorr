package com.github.schaka.janitorr.servarr.bazarr

import feign.Param
import feign.RequestLine

interface BazarrClient {

    @RequestLine("GET /movies?start=0&length=-1&radarrid[]={movieId}")
    fun getMovieSubtitles(@Param("movieId") movieId: Int): BazarrPage

    @RequestLine("GET /episodes?seriesid[]={showId}")
    fun getTvSubtitles(@Param("showId") showId: Int): BazarrPage

    @RequestLine("GET /episodes?seriesid[]={showId}&episodeid[]={episodeIds}")
    fun getTvSubtitles(@Param("showId") showId: Int, @Param("episodeIds") episodeIds: List<Int>): BazarrPage

}