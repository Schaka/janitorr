package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.api.User
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.items.ItemPage
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.bazarr.BazarrService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class MediaRestServiceTest {

    @InjectMockKs
    lateinit var jellyfinRestService: JellyfinRestService

    @MockK
    lateinit var mediaServerClient: MediaServerClient

    @MockK
    lateinit var mediaServerUserClient: MediaServerUserClient

    @MockK
    lateinit var bazarrService: BazarrService

    @MockK
    lateinit var jellyfinProperties: JellyfinProperties

    @MockK
    lateinit var applicationProperties: ApplicationProperties

    @SpyK
    var fileSystemProperties: FileSystemProperties = FileSystemProperties(
        access = true,
        leavingSoonDir = "/data/media/leaving-soon",
        mediaServerLeavingSoonDir = "/data/media/leaving-soon",
        validateSeeding = true,
        fromScratch = true
    )

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
    fun testExtendedMovieStructure() {
        val movie = LibraryItem(
            1,
            LocalDateTime.now().minusDays(14),
            "/data/torrents/movies/movie-folder/movie.mkv",
            "/data/media/movies/m/movie [imdb-812543]/movie.mkv",

            "/data/media/movies/m/movie [imdb-812543]",
            "/data/media/movies",
            "/data/media/movies/m/movie [imdb-812543]/movie.mkv",

            "812543"

        )

        val path = Path.of(fileSystemProperties.leavingSoonDir, "movies")
        val structure = jellyfinRestService.pathStructure(movie, path)

        assertEquals(Path.of("/data/media/movies/m/movie [imdb-812543]"), structure.sourceFolder)
        assertEquals(Path.of("/data/media/movies/m/movie [imdb-812543]/movie.mkv"), structure.sourceFile)
        assertEquals(Path.of("/data/media/leaving-soon/movies/m/movie [imdb-812543]"), structure.targetFolder)
        assertEquals(Path.of("/data/media/leaving-soon/movies/m/movie [imdb-812543]/movie.mkv"), structure.targetFile)
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
    fun testExtendedTvStructure() {
        val episode = LibraryItem(
            1,
            LocalDateTime.now().minusDays(14),
            "/data/torrents/tv/tv-show-folder-season 01/ep01.mkv",
            "/data/media/tv/t/tv-show [imdb-812543]/season 01/ep01.mkv",

            "/data/media/tv/t/tv-show [imdb-812543]",
            "/data/media/tv",
            "/data/media/tv/t/tv-show [imdb-812543]/season 01/ep01.mkv",

            "812543"

        )

        val path = Path.of(fileSystemProperties.leavingSoonDir, "tv")
        val structure = jellyfinRestService.pathStructure(episode, path)

        assertEquals(Path.of("/data/media/tv/t/tv-show [imdb-812543]"), structure.sourceFolder)
        assertEquals(Path.of("/data/media/tv/t/tv-show [imdb-812543]/season 01"), structure.sourceFile)
        assertEquals(Path.of("/data/media/leaving-soon/tv/t/tv-show [imdb-812543]"), structure.targetFolder)
        assertEquals(Path.of("/data/media/leaving-soon/tv/t/tv-show [imdb-812543]/season 01"), structure.targetFile)
    }

    @Test
    fun testMetadataParsing() {
        assertEquals(4513, jellyfinRestService.parseMetadataId("4513-30-days-of-night"))
        assertEquals(4513, jellyfinRestService.parseMetadataId("4513"))
        assertNull(jellyfinRestService.parseMetadataId(null))
    }

    @Test
    fun testIsItemFavorited_whenItemInFavorites_returnsTrue() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            mediaServerIds = mutableListOf("jellyfin-id-123", "jellyfin-id-456")
        )
        val favoritedIds = setOf("jellyfin-id-123", "jellyfin-id-789")

        val result = jellyfinRestService.isItemFavorited(item, favoritedIds)

        assertEquals(true, result)
    }

    @Test
    fun testIsItemFavorited_whenItemNotInFavorites_returnsFalse() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            mediaServerIds = mutableListOf("jellyfin-id-999", "jellyfin-id-888")
        )
        val favoritedIds = setOf("jellyfin-id-123", "jellyfin-id-789")

        val result = jellyfinRestService.isItemFavorited(item, favoritedIds)

        assertEquals(false, result)
    }

    @Test
    fun testIsItemFavorited_whenItemHasNoMediaServerIds_returnsFalse() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            mediaServerIds = mutableListOf()
        )
        val favoritedIds = setOf("jellyfin-id-123", "jellyfin-id-789")

        val result = jellyfinRestService.isItemFavorited(item, favoritedIds)

        assertEquals(false, result)
    }

    @Test
    fun testGetAllFavoritedItemIds_whenMediaServerDisabled_returnsEmptySet() {
        every { jellyfinProperties.enabled } returns false
        every { jellyfinProperties.excludeFavorited } returns true

        val result = jellyfinRestService.getAllFavoritedItemIds()

        assertTrue(result.isEmpty())
    }

    @Test
    fun testGetAllFavoritedItemIds_withMultipleUsers_aggregatesAllFavorites() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = User("User1", "user-id-1")
        val user2 = User("User2", "user-id-2")
        every { mediaServerClient.listUsers() } returns listOf(user1, user2)

        val movie1 = mockk<LibraryContent>(relaxed = true) { every { Id } returns "movie-123" }
        val movie2 = mockk<LibraryContent>(relaxed = true) { every { Id } returns "movie-456" }
        val movie3 = mockk<LibraryContent>(relaxed = true) { every { Id } returns "movie-789" }

        every { mediaServerClient.getUserFavorites("user-id-1") } returns ItemPage(listOf(movie1, movie2), 0, 2)
        every { mediaServerClient.getUserFavorites("user-id-2") } returns ItemPage(listOf(movie3), 0, 1)

        val result = jellyfinRestService.getAllFavoritedItemIds()

        assertEquals(3, result.size)
        assertTrue(result.contains("movie-123"))
        assertTrue(result.contains("movie-456"))
        assertTrue(result.contains("movie-789"))
    }

    @Test
    fun testGetAllFavoritedItemIds_whenUserApiFails_continuesWithOtherUsers() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = User("User1", "user-id-1")
        val user2 = User("User2", "user-id-2")
        every { mediaServerClient.listUsers() } returns listOf(user1, user2)

        every { mediaServerClient.getUserFavorites("user-id-1") } throws RuntimeException("API Error")

        val movie3 = mockk<LibraryContent>(relaxed = true) { every { Id } returns "movie-789" }
        every { mediaServerClient.getUserFavorites("user-id-2") } returns ItemPage(listOf(movie3), 0, 1)

        val result = jellyfinRestService.getAllFavoritedItemIds()

        assertEquals(1, result.size)
        assertTrue(result.contains("movie-789"))
    }

    @Test
    fun testGetAllFavoritedItemIds_whenNoUsers_returnsEmptySet() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true
        every { mediaServerClient.listUsers() } returns emptyList()

        val result = jellyfinRestService.getAllFavoritedItemIds()

        assertTrue(result.isEmpty())
    }

    @Test
    fun testIsItemFavorited_withMultipleMediaServerIds_checksAll() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            mediaServerIds = mutableListOf("jellyfin-id-111", "jellyfin-id-222", "jellyfin-id-333")
        )
        val favoritedIds = setOf("jellyfin-id-222", "jellyfin-id-999")

        val result = jellyfinRestService.isItemFavorited(item, favoritedIds)

        assertEquals(true, result)
    }

}