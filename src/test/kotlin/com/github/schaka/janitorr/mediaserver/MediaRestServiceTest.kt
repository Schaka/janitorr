package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.api.User
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.ProviderIds
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
    fun testIsItemFavorited_whenItemMatchesByImdb_returnsTrue() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            imdbId = "tt0115963"
        )
        val favoritedItems = listOf(
            FavoriteItem("jellyfin-id-123", "tt0115963", 9100, null),
            FavoriteItem("jellyfin-id-456", "tt1234567", 5678, null)
        )

        val result = jellyfinRestService.isItemFavorited(item, favoritedItems)

        assertEquals(true, result)
    }

    @Test
    fun testIsItemFavorited_whenItemMatchesByTmdb_returnsTrue() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            tmdbId = 9100
        )
        val favoritedItems = listOf(
            FavoriteItem("jellyfin-id-123", "tt0115963", 9100, null)
        )

        val result = jellyfinRestService.isItemFavorited(item, favoritedItems)

        assertEquals(true, result)
    }

    @Test
    fun testIsItemFavorited_whenItemMatchesByTvdb_returnsTrue() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            tvdbId = 376459
        )
        val favoritedItems = listOf(
            FavoriteItem("jellyfin-id-123", "tt11704040", 99353, 376459)
        )

        val result = jellyfinRestService.isItemFavorited(item, favoritedItems)

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
            imdbId = "tt9999999",
            tmdbId = 999999
        )
        val favoritedItems = listOf(
            FavoriteItem("jellyfin-id-123", "tt0115963", 9100, null)
        )

        val result = jellyfinRestService.isItemFavorited(item, favoritedItems)

        assertEquals(false, result)
    }

    @Test
    fun testIsItemFavorited_whenItemHasNoProviderIds_returnsFalse() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file"
        )
        val favoritedItems = listOf(
            FavoriteItem("jellyfin-id-123", "tt0115963", 9100, null)
        )

        val result = jellyfinRestService.isItemFavorited(item, favoritedItems)

        assertEquals(false, result)
    }

    @Test
    fun testGetAllFavoritedItems_whenMediaServerDisabled_returnsEmptyList() {
        every { jellyfinProperties.enabled } returns false
        every { jellyfinProperties.excludeFavorited } returns true

        val result = jellyfinRestService.getAllFavoritedItems()

        assertTrue(result.isEmpty())
    }

    @Test
    fun testGetAllFavoritedItems_withMultipleUsers_aggregatesAllFavorites() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = User("User1", "user-id-1")
        val user2 = User("User2", "user-id-2")
        every { mediaServerClient.listUsers() } returns listOf(user1, user2)

        val providerIds1 = ProviderIds("12345", "tt0115963", "9100")
        val providerIds2 = ProviderIds(null, "tt1234567", "5678")
        val providerIds3 = ProviderIds("54321", "tt7654321", "8765")

        val movie1 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-123"
            every { ProviderIds } returns providerIds1
        }
        val movie2 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-456"
            every { ProviderIds } returns providerIds2
        }
        val movie3 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-789"
            every { ProviderIds } returns providerIds3
        }

        every { mediaServerClient.getUserFavorites("user-id-1") } returns ItemPage(listOf(movie1, movie2), 0, 2)
        every { mediaServerClient.getUserFavorites("user-id-2") } returns ItemPage(listOf(movie3), 0, 1)

        val result = jellyfinRestService.getAllFavoritedItems()

        assertEquals(3, result.size)
        assertEquals("tt0115963", result[0].imdbId)
        assertEquals(9100, result[0].tmdbId)
        assertEquals(12345, result[0].tvdbId)
    }

    @Test
    fun testGetAllFavoritedItems_whenUserApiFails_continuesWithOtherUsers() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = User("User1", "user-id-1")
        val user2 = User("User2", "user-id-2")
        every { mediaServerClient.listUsers() } returns listOf(user1, user2)

        every { mediaServerClient.getUserFavorites("user-id-1") } throws RuntimeException("API Error")

        val providerIds3 = ProviderIds("54321", "tt7654321", "8765")
        val movie3 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-789"
            every { ProviderIds } returns providerIds3
        }
        every { mediaServerClient.getUserFavorites("user-id-2") } returns ItemPage(listOf(movie3), 0, 1)

        val result = jellyfinRestService.getAllFavoritedItems()

        assertEquals(1, result.size)
        assertEquals("movie-789", result[0].jellyfinId)
    }

    @Test
    fun testGetAllFavoritedItems_whenNoUsers_returnsEmptyList() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true
        every { mediaServerClient.listUsers() } returns emptyList()

        val result = jellyfinRestService.getAllFavoritedItems()

        assertTrue(result.isEmpty())
    }

}