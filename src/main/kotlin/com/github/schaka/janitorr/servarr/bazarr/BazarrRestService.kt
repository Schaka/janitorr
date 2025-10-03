package com.github.schaka.janitorr.servarr.bazarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class BazarrRestService(

    val bazarrClient: BazarrClient,

    val applicationProperties: ApplicationProperties,

    val fileSystemProperties: FileSystemProperties,

    val bazarrProperties: BazarrProperties,

) : BazarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        const val CACHE_NAME_MOVIES = "bazarr-movie-cache"
        const val CACHE_NAME_TV = "bazarr-tv-cache"

    }

    @Cacheable(CACHE_NAME_MOVIES)
    override fun getSubtitlesForMovies(movieId: Int): List<BazarrPayload> {
        val result = bazarrClient.getMovieSubtitles(movieId)

        return result.data
    }

    @Cacheable(CACHE_NAME_TV)
    override fun getSubtitlesForTv(showId: Int): List<BazarrPayload>  {
        val result = bazarrClient.getTvSubtitles(showId)

        return result.data
    }
}