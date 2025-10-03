package com.github.schaka.janitorr.external.common

import com.github.schaka.janitorr.servarr.LibraryItem
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ExternalDataServiceTest {

    private lateinit var tmdbService: TmdbService
    private lateinit var omdbService: OmdbService
    private lateinit var traktService: TraktService
    private lateinit var externalDataProperties: ExternalDataProperties
    private lateinit var externalDataService: ExternalDataService

    @BeforeEach
    fun setup() {
        tmdbService = mockk()
        omdbService = mockk()
        traktService = mockk()
        externalDataProperties = ExternalDataProperties(
            enabled = true,
            scoring = ScoringWeights()
        )
        externalDataService = ExternalDataService(
            externalDataProperties,
            tmdbService,
            omdbService,
            traktService
        )
    }

    @Test
    fun `enrichMediaData should return empty intelligence when APIs are disabled`() {
        val disabledProperties = ExternalDataProperties(enabled = false)
        val service = ExternalDataService(disabledProperties)

        val item = createTestLibraryItem()
        val result = service.enrichMediaData(item)

        assertEquals(0.0, result.overallScore)
        assertNull(result.tmdbRating)
        assertNull(result.imdbRating)
    }

    @Test
    fun `enrichMediaData should aggregate data from all APIs`() {
        val item = createTestLibraryItem()

        every { tmdbService.getRating(item) } returns 8.5
        every { tmdbService.getPopularityScore(item) } returns 75.0
        every { tmdbService.isTrending(item) } returns true
        every { omdbService.getRating(item) } returns 8.2
        every { traktService.getWatcherCount(item) } returns 1500
        every { traktService.getCollectorCount(item) } returns 5000

        val result = externalDataService.enrichMediaData(item)

        assertEquals(8.5, result.tmdbRating)
        assertEquals(8.2, result.imdbRating)
        assertEquals(75.0, result.popularityScore)
        assertNotNull(result.trendingScore)
        assertTrue(result.overallScore > 0)
    }

    @Test
    fun `shouldPreserveMedia returns true for high IMDb rating`() {
        val intelligence = MediaIntelligence(imdbRating = 8.5, overallScore = 50.0)
        assertTrue(externalDataService.shouldPreserveMedia(intelligence))
    }

    @Test
    fun `shouldPreserveMedia returns true for high TMDB rating`() {
        val intelligence = MediaIntelligence(tmdbRating = 8.5, overallScore = 50.0)
        assertTrue(externalDataService.shouldPreserveMedia(intelligence))
    }

    @Test
    fun `shouldPreserveMedia returns true for trending content`() {
        val intelligence = MediaIntelligence(trendingScore = 80.0, overallScore = 50.0)
        assertTrue(externalDataService.shouldPreserveMedia(intelligence))
    }

    @Test
    fun `shouldPreserveMedia returns true for collectible content`() {
        val intelligence = MediaIntelligence(collectibilityScore = 85.0, overallScore = 50.0)
        assertTrue(externalDataService.shouldPreserveMedia(intelligence))
    }

    @Test
    fun `shouldPreserveMedia returns true for high overall score`() {
        val intelligence = MediaIntelligence(overallScore = 75.0)
        assertTrue(externalDataService.shouldPreserveMedia(intelligence))
    }

    @Test
    fun `shouldPreserveMedia returns false for low scores`() {
        val intelligence = MediaIntelligence(
            tmdbRating = 5.0,
            imdbRating = 5.5,
            overallScore = 50.0
        )
        assertFalse(externalDataService.shouldPreserveMedia(intelligence))
    }

    private fun createTestLibraryItem(): LibraryItem {
        return LibraryItem(
            id = 1,
            importedDate = LocalDateTime.now().minusDays(30),
            originalPath = "/movies/Test Movie (2020)/Test Movie.mkv",
            libraryPath = "/movies/Test Movie (2020)/Test Movie.mkv",
            parentPath = "/movies/Test Movie (2020)",
            rootFolderPath = "/movies",
            filePath = "/movies/Test Movie (2020)",
            imdbId = "tt1234567",
            tmdbId = 12345,
            tvdbId = null,
            season = null
        )
    }
}
