package com.github.schaka.janitorr.servarr.bazarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.HistorySort
import com.github.schaka.janitorr.servarr.HistorySort.MOST_RECENT
import com.github.schaka.janitorr.servarr.HistorySort.OLDEST
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import com.github.schaka.janitorr.servarr.radarr.movie.MovieFile
import com.github.schaka.janitorr.servarr.radarr.movie.MoviePayload
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.exists

@Service
@RegisterReflectionForBinding(classes = [BazarrPage::class, BazarrPayload::class])
class BazarrRestService(

    val bazarrClient: BazarrClient,

    val applicationProperties: ApplicationProperties,

    val fileSystemProperties: FileSystemProperties,

    val bazarrProperties: BazarrProperties,

) : BazarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

    }

    override fun getSubtitlesForMovies(movieId: Int): List<BazarrPayload> {
        TODO("Not yet implemented")
    }

    override fun getSubtitlesForTv(showId: Int, episodeId: Int): List<BazarrPayload>  {
        TODO("Not yet implemented")
    }
}