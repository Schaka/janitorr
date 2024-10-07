package com.github.schaka.janitorr.servarr.bazarr

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BazarrNoOpService : BazarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun getSubtitlesForMovies(movieId: Int): List<BazarrPayload> {
        log.info("Bazarr is disabled, no tv episode subtitles")
        return emptyList()
    }

    override fun getSubtitlesForTv(showId: Int): List<BazarrPayload> {
        log.info("Bazarr is disabled, no movie subtitles")
        return emptyList()
    }

}