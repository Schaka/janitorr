package com.github.schaka.janitorr.servarr.bazarr

import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BazarrNoOpService : BazarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun getSubtitlesForMovies(movieId: Int): List<LibraryItem> {
        log.info("Bazarr is disabled, not getting any movies")
        return listOf()
    }

    override fun getSubtitlesForTv(showId: Int, episodeId: Int) {
        log.info("Bazarr is disabled, not deleting any movies")
    }

}