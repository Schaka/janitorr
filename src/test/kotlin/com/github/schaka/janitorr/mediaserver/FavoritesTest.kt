package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.api.MediaServerUser
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.LibraryType
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class FavoritesTest {

    @InjectMockKs
    lateinit var jellyfinRestService: JellyfinRestService

    @MockK
    lateinit var mediaServerLibraryQueryService: MediaServerLibraryQueryService

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

        val user1 = MediaServerUser("User1", "user-id-1")
        val user2 = MediaServerUser("User2", "user-id-2")
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
        assertEquals("tt0115963", result[0].ProviderIds?.Imdb)
        assertEquals("9100", result[0].ProviderIds?.Tmdb)
        assertEquals("12345", result[0].ProviderIds?.Tvdb)
    }

    @Test
    fun testGetAllFavoritedItems_whenUserApiFails_continuesWithOtherUsers() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = MediaServerUser("User1", "user-id-1")
        val user2 = MediaServerUser("User2", "user-id-2")
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
        assertEquals("movie-789", result[0].Id)
    }

    @Test
    fun testGetAllFavoritedItems_whenNoUsers_returnsEmptyList() {
        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true
        every { mediaServerClient.listUsers() } returns emptyList()

        val result = jellyfinRestService.getAllFavoritedItems()

        assertTrue(result.isEmpty())
    }

    @Test
    fun when_item_is_favorited_filter_out_by_imdb() {
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

        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = MediaServerUser("User1", "user-id-1")
        every { mediaServerClient.listUsers() } returns listOf(user1)

        val providerIds1 = ProviderIds("12345", "tt0115963", "9100")
        val providerIds2 = ProviderIds(null, "tt1234567", "5678")

        val movie1 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-123"
            every { Type } returns "movie"
            every { ProviderIds } returns providerIds1
        }
        val movie2 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-456"
            every { Type } returns "movie"
            every { ProviderIds } returns providerIds2
        }

        every { mediaServerClient.getUserFavorites("user-id-1") } returns ItemPage(listOf(movie1, movie2), 0, 2)

        val result = jellyfinRestService.filterOutFavorites(listOf(item), LibraryType.MOVIES)

        assertEquals(listOf(), result)
    }

    @Test
    fun when_item_is_favorited_filter_out_by_tmdb() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            imdbId = "tt0115964", // don't match imdb this time
            tmdbId = 11005
        )

        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = MediaServerUser("User1", "user-id-1")
        every { mediaServerClient.listUsers() } returns listOf(user1)

        val providerIds1 = ProviderIds("12345", "tt0115963", "11005")
        val providerIds2 = ProviderIds(null, "tt1234567", "5678")

        val movie1 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-123"
            every { Type } returns "movie"
            every { ProviderIds } returns providerIds1
        }
        val movie2 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "movie-456"
            every { Type } returns "movie"
            every { ProviderIds } returns providerIds2
        }

        every { mediaServerClient.getUserFavorites("user-id-1") } returns ItemPage(listOf(movie1, movie2), 0, 2)

        val result = jellyfinRestService.filterOutFavorites(listOf(item), LibraryType.MOVIES)

        assertEquals(listOf(), result)
    }

    @Test
    fun when_item_is_favorited_filter_out_by_tvdb() {
        val item = LibraryItem(
            1,
            LocalDateTime.now(),
            "/path/original",
            "/path/library",
            "/path/parent",
            "/path/root",
            "/path/file",
            imdbId = "tt0115964", // don't match imdb this time
            tvdbId = 12345
        )

        every { jellyfinProperties.enabled } returns true
        every { jellyfinProperties.excludeFavorited } returns true

        val user1 = MediaServerUser("User1", "user-id-1")
        every { mediaServerClient.listUsers() } returns listOf(user1)

        val providerIds1 = ProviderIds("12345", "tt0115963", null)
        val providerIds2 = ProviderIds("123456", "tt1234567")

        val show1 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "tv-123"
            every { Type } returns "series"
            every { ProviderIds } returns providerIds1
        }
        val show2 = mockk<LibraryContent>(relaxed = true) {
            every { Id } returns "tv-456"
            every { Type } returns "series"
            every { ProviderIds } returns providerIds2
        }

        every { mediaServerClient.getUserFavorites("user-id-1") } returns ItemPage(listOf(show1, show2), 0, 2)

        val result = jellyfinRestService.filterOutFavorites(listOf(item), LibraryType.TV_SHOWS)

        assertEquals(listOf(), result)
    }
}