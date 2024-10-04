package com.github.schaka.janitorr.servarr.bazarr

interface BazarrService {

    fun getSubtitlesForMovies(movieId: Int): List<BazarrPayload>

    fun getSubtitlesForTv(showId: Int, episodeId: Int): List<BazarrPayload>
}