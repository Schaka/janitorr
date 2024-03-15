package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.servarr.LibraryItem
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
internal class MediaRestServiceTest {

    @InjectMockKs
    lateinit var jellyfinRestService: JellyfinRestService

    @MockK
    lateinit var mediaServerClient: MediaServerClient
    @MockK
    lateinit var mediaServerUserClient: MediaServerUserClient
    @MockK
    lateinit var jellyfinProperties: JellyfinProperties
    @MockK
    lateinit var applicationProperties: ApplicationProperties

    @SpyK
    var fileSystemProperties: FileSystemProperties = FileSystemProperties("/data/media/leaving-soon", true, true)

    @Test
    fun testMovieStructure() {

        val movie = LibraryItem(
            1,
            LocalDateTime.now().minusDays(14),
            "/data/torrents/movies/movie-folder/movie.mkv",
            "/data/media/movies/movie [imdb-812543]/movie.mkv",

            "/data/media/movies/movie [imdb-812543]",
            "/data/media/movies",
            "/data/media/movies/movie [imdb-812543]/movie.mkv",

            "812543"

        )

        val path = Path.of(fileSystemProperties.leavingSoonDir, "movies")
        val structure = jellyfinRestService.pathStructure(movie, path)

        assertEquals(Path.of("/data/media/movies/movie [imdb-812543]"), structure.sourceFolder)
        assertEquals(Path.of("/data/media/movies/movie [imdb-812543]/movie.mkv"), structure.sourceFile)
        assertEquals(Path.of("/data/media/leaving-soon/movies/movie [imdb-812543]"), structure.targetFolder)
        assertEquals(Path.of("/data/media/leaving-soon/movies/movie [imdb-812543]/movie.mkv"), structure.targetFile)
    }

    @Test
    fun testTvStructure() {

        val episode = LibraryItem(
            1,
            LocalDateTime.now().minusDays(14),
            "/data/torrents/tv/tv-show-folder-season 01/ep01.mkv",
            "/data/media/tv/tv-show [imdb-812543]/season 01/ep01.mkv",

            "/data/media/tv/tv-show [imdb-812543]",
            "/data/media/tv",
            "/data/media/tv/tv-show [imdb-812543]/season 01/ep01.mkv",

            "812543"

        )

        val path = Path.of(fileSystemProperties.leavingSoonDir, "tv")
        val structure = jellyfinRestService.pathStructure(episode, path)

        assertEquals(Path.of("/data/media/tv/tv-show [imdb-812543]"), structure.sourceFolder)
        assertEquals(Path.of("/data/media/tv/tv-show [imdb-812543]/season 01"), structure.sourceFile)
        assertEquals(Path.of("/data/media/leaving-soon/tv/tv-show [imdb-812543]"), structure.targetFolder)
        assertEquals(Path.of("/data/media/leaving-soon/tv/tv-show [imdb-812543]/season 01"), structure.targetFile)
    }

    @Test
    fun testMetadataParsing() {
        assertEquals(4513, jellyfinRestService.parseMetadataId("4513-30-days-of-night"))
        assertEquals(4513, jellyfinRestService.parseMetadataId( "4513"))
        assertNull(jellyfinRestService.parseMetadataId(null))
    }

}