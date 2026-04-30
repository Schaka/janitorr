package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.config.*
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.ProviderIds
import com.github.schaka.janitorr.mediaserver.library.items.ItemPage
import com.github.schaka.janitorr.mediaserver.library.items.MediaFolderItem
import com.github.schaka.janitorr.mediaserver.lookup.MediaLookup
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.bazarr.BazarrService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

/**
 * Tests for [BaseMediaServerService.getMediaServerIdsForLibrary] to ensure all Jellyfin IDs
 * are correctly resolved for use by stats services (Jellystat, Streamystats).
 *
 * Related issues:
 * - https://github.com/Schaka/janitorr/issues/236 (TV show watched date not picked up)
 * - https://github.com/Schaka/janitorr/issues/227 (broken last watched date)
 */
@ExtendWith(MockKExtension::class)
internal class GetMediaServerIdsForLibraryTest {

    @MockK
    lateinit var mediaServerClient: MediaServerClient

    @MockK
    lateinit var mediaServerUserClient: MediaServerUserClient

    @MockK
    lateinit var bazarrService: BazarrService

    @SpyK
    var mediaServerLibraryQueryService: MediaServerLibraryQueryService = MediaServerLibraryQueryService()

    @MockK
    lateinit var jellyfinProperties: JellyfinProperties

    @SpyK
    var fileSystemProperties: FileSystemProperties = FileSystemProperties(false, "/data/media/leaving-soon", "/data/media/leaving-soon", true, true)

    lateinit var jellyfinRestService: JellyfinRestService

    private val parentFolder = mockk<MediaFolderItem> {
        every { Id } returns "parent-folder-id"
        every { Type } returns "CollectionFolder"
        every { Name } returns "library"
    }

    @BeforeEach
    fun setUp() {
        every { mediaServerClient.getAllItems() } returns ItemPage(listOf(parentFolder), 0, 1)
    }

    private fun createService(wholeTvShow: Boolean = false): JellyfinRestService {
        val appProps = ApplicationProperties(
            mediaDeletion = MediaDeletion(),
            tagBasedDeletion = TagDeletion(),
            episodeDeletion = EpisodeDeletion(),
            wholeTvShow = wholeTvShow
        )
        return JellyfinRestService(
            mediaServerClient,
            mediaServerUserClient,
            bazarrService,
            mediaServerLibraryQueryService,
            jellyfinProperties,
            appProps,
            fileSystemProperties
        )
    }

    private fun libraryItem(
        id: Int,
        imdbId: String? = null,
        tvdbId: Int? = null,
        tmdbId: Int? = null,
        season: Int? = null
    ): LibraryItem {
        return LibraryItem(
            id = id,
            importedDate = LocalDateTime.now().minusDays(14),
            originalPath = "/data/torrents/media/folder/file.mkv",
            libraryPath = "/data/media/library/folder/file.mkv",
            parentPath = "/data/media/library/folder",
            rootFolderPath = "/data/media/library",
            filePath = "/data/media/library/folder/file.mkv",
            imdbId = imdbId,
            tvdbId = tvdbId,
            tmdbId = tmdbId,
            season = season
        )
    }

    private fun movieContent(id: String, providerIds: ProviderIds? = null): LibraryContent {
        return LibraryContent(
            Id = id,
            Type = "Movie",
            IsFolder = false,
            IsMovie = true,
            IsSeries = false,
            Name = "Movie $id",
            ProviderIds = providerIds
        )
    }

    private fun seriesContent(id: String, providerIds: ProviderIds? = null): LibraryContent {
        return LibraryContent(
            Id = id,
            Type = "Series",
            IsFolder = true,
            IsMovie = false,
            IsSeries = true,
            Name = "Show $id",
            ProviderIds = providerIds
        )
    }

    private fun seasonContent(
        id: String,
        seasonName: String,
        indexNumber: Int,
        seriesId: String,
        providerIds: ProviderIds? = null
    ): LibraryContent {
        return LibraryContent(
            Id = id,
            Type = "Season",
            IsFolder = true,
            IsMovie = false,
            IsSeries = false,
            Name = seasonName,
            IndexNumber = indexNumber,
            SeriesId = seriesId,
            SeriesName = "Show $seriesId",
            ProviderIds = providerIds
        )
    }

    // ========== Movie Tests ==========

    @Nested
    inner class Movies {

        @Test
        fun `movie matched by IMDB ID returns its Jellyfin ID`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", ProviderIds(Imdb = "tt1234567"))),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt1234567")),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactly("jf-100")
        }

        @Test
        fun `movie matched by TMDB ID returns its Jellyfin ID`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-200", ProviderIds(Tmdb = "55555"))),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, tmdbId = 55555)),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactly("jf-200")
        }

        @Test
        fun `movie matched by TVDB ID returns its Jellyfin ID`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-300", ProviderIds(Tvdb = "77777"))),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, tvdbId = 77777)),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactly("jf-300")
        }

        @Test
        fun `movie with no matching Jellyfin entry returns empty list`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", ProviderIds(Imdb = "tt9999999"))),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt0000001")),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).isEmpty()
        }

        @Test
        fun `duplicate movies in Jellyfin both return their IDs`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(
                    movieContent("jf-100", ProviderIds(Imdb = "tt1234567")),
                    movieContent("jf-101", ProviderIds(Imdb = "tt1234567"))
                ),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt1234567")),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactlyInAnyOrder("jf-100", "jf-101")
        }

        @Test
        fun `multiple movies each resolve to correct Jellyfin IDs`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(
                    movieContent("jf-100", ProviderIds(Imdb = "tt1111111")),
                    movieContent("jf-200", ProviderIds(Imdb = "tt2222222")),
                    movieContent("jf-300", ProviderIds(Imdb = "tt3333333"))
                ),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(
                    libraryItem(1, imdbId = "tt1111111"),
                    libraryItem(2, imdbId = "tt2222222"),
                    libraryItem(3, imdbId = "tt3333333")
                ),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactly("jf-100")
            assertThat(result[MediaLookup(2)]).containsExactly("jf-200")
            assertThat(result[MediaLookup(3)]).containsExactly("jf-300")
        }

        @Test
        fun `movie matched by TMDB when TMDB ID contains extra text is still resolved`() {
            val service = createService()
            // Jellyfin sometimes stores provider IDs like "4513-30-days-of-night"
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", ProviderIds(Tmdb = "4513-30-days-of-night"))),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, tmdbId = 4513)),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactly("jf-100")
        }

        @Test
        fun `movie with multiple provider IDs matches on any of them`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", ProviderIds(Imdb = "tt1111111", Tmdb = "22222", Tvdb = "33333"))),
                0, 1
            )

            // Match by TMDB even though IMDB doesn't match
            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt0000000", tmdbId = 22222)),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactly("jf-100")
        }

        @Test
        fun `movies across multiple parent folders are all discovered`() {
            val secondParent = mockk<MediaFolderItem> {
                every { Id } returns "second-parent-id"
                every { Type } returns "CollectionFolder"
                every { Name } returns "4k-movies"
            }
            every { mediaServerClient.getAllItems() } returns ItemPage(listOf(parentFolder, secondParent), 0, 2)

            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", ProviderIds(Imdb = "tt1111111"))),
                0, 1
            )
            every { mediaServerClient.getAllMovies("second-parent-id") } returns ItemPage(
                listOf(movieContent("jf-200", ProviderIds(Imdb = "tt1111111"))),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt1111111")),
                LibraryType.MOVIES, false
            )

            // The same movie exists in two libraries (e.g. 1080p + 4K) - both IDs must be returned
            assertThat(result[MediaLookup(1)]).containsExactlyInAnyOrder("jf-100", "jf-200")
        }
    }

    // ========== TV Show Tests (by season) ==========

    @Nested
    inner class TvShowsBySeason {

        @Test
        fun `tv show season matched by IndexNumber returns season Jellyfin ID`() {
            val showProviderIds = ProviderIds(Imdb = "tt1111111", Tmdb = "100")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(
                    seasonContent("jf-season-1", "Season 1", 1, "jf-show-1"),
                    seasonContent("jf-season-2", "Season 2", 2, "jf-show-1")
                ),
                0, 2
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt1111111", season = 1)),
                LibraryType.TV_SHOWS, true
            )

            assertThat(result[MediaLookup(10, 1)]).containsExactly("jf-season-1")
        }

        @Test
        fun `tv show season matched by name pattern returns season Jellyfin ID`() {
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            // Season name uses language-agnostic pattern "Word Number" e.g. "Season 3"
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(
                    seasonContent("jf-season-3", "Season 3", 3, "jf-show-1")
                ),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt1111111", season = 3)),
                LibraryType.TV_SHOWS, true
            )

            assertThat(result[MediaLookup(10, 3)]).containsExactly("jf-season-3")
        }

        @Test
        fun `multiple seasons of same show each get correct Jellyfin IDs`() {
            val showProviderIds = ProviderIds(Tmdb = "500")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(
                    seasonContent("jf-s1", "Season 1", 1, "jf-show-1"),
                    seasonContent("jf-s2", "Season 2", 2, "jf-show-1"),
                    seasonContent("jf-s3", "Season 3", 3, "jf-show-1")
                ),
                0, 3
            )

            val items = listOf(
                libraryItem(10, tmdbId = 500, season = 1),
                libraryItem(10, tmdbId = 500, season = 2),
                libraryItem(10, tmdbId = 500, season = 3)
            )

            val result = service.getMediaServerIdsForLibrary(items, LibraryType.TV_SHOWS, true)

            assertThat(result[MediaLookup(10, 1)]).containsExactly("jf-s1")
            assertThat(result[MediaLookup(10, 2)]).containsExactly("jf-s2")
            assertThat(result[MediaLookup(10, 3)]).containsExactly("jf-s3")
        }

        @Test
        fun `season with no matching show returns empty list`() {
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", ProviderIds(Imdb = "tt9999999"))),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(seasonContent("jf-s1", "Season 1", 1, "jf-show-1")),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt0000001", season = 1)),
                LibraryType.TV_SHOWS, true
            )

            assertThat(result[MediaLookup(10, 1)]).isEmpty()
        }

        @Test
        fun `season inherits provider IDs from parent series for matching`() {
            // This is critical: MediaServerLibraryQueryService copies show ProviderIds onto seasons
            // Without this, seasons would have no IMDB/TMDB to match against
            val showProviderIds = ProviderIds(Imdb = "tt5555555", Tmdb = "9999")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            // Seasons initially have null provider IDs - the query service overrides them with the show's
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(seasonContent("jf-s2", "Season 2", 2, "jf-show-1", providerIds = null)),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt5555555", season = 2)),
                LibraryType.TV_SHOWS, true
            )

            // The season should be found because MediaServerLibraryQueryService propagates show ProviderIds to seasons
            assertThat(result[MediaLookup(10, 2)]).containsExactly("jf-s2")
        }

        @Test
        fun `non-English season names still match via language-agnostic pattern`() {
            // The regex is: (\w+) (?<season>\d+) - matches "Staffel 2", "Saison 2", "Temporada 2", etc.
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(
                    seasonContent("jf-staffel-2", "Staffel 2", 2, "jf-show-1")
                ),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt1111111", season = 2)),
                LibraryType.TV_SHOWS, true
            )

            assertThat(result[MediaLookup(10, 2)]).containsExactly("jf-staffel-2")
        }

        @Test
        fun `multiple episodes of same season grouped under single MediaLookup key`() {
            val showProviderIds = ProviderIds(Tmdb = "100")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(seasonContent("jf-s1", "Season 1", 1, "jf-show-1")),
                0, 1
            )

            // Two library items with same sonarr ID and season (e.g. two episodes from same season)
            val items = listOf(
                libraryItem(10, tmdbId = 100, season = 1),
                libraryItem(10, tmdbId = 100, season = 1)
            )

            val result = service.getMediaServerIdsForLibrary(items, LibraryType.TV_SHOWS, true)

            // Both episodes group under the same lookup key, IDs collected from both
            assertThat(result[MediaLookup(10, 1)]).contains("jf-s1")
        }

        @Test
        fun `shows across multiple parent folders are all discovered`() {
            val secondParent = mockk<MediaFolderItem> {
                every { Id } returns "anime-folder-id"
                every { Type } returns "CollectionFolder"
                every { Name } returns "anime"
            }
            every { mediaServerClient.getAllItems() } returns ItemPage(listOf(parentFolder, secondParent), 0, 2)

            val showProviderIds = ProviderIds(Tmdb = "100")
            val service = createService(wholeTvShow = false)

            // Same show appears in both "tv" and "anime" libraries
            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllTvShows("anime-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-2", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(seasonContent("jf-s1a", "Season 1", 1, "jf-show-1")),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-2") } returns ItemPage(
                listOf(seasonContent("jf-s1b", "Season 1", 1, "jf-show-2")),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, tmdbId = 100, season = 1)),
                LibraryType.TV_SHOWS, true
            )

            assertThat(result[MediaLookup(10, 1)]).containsExactlyInAnyOrder("jf-s1a", "jf-s1b")
        }
    }

    // ========== TV Show Tests (whole show, not by season) ==========

    @Nested
    inner class TvShowsWholeShow {

        @Test
        fun `whole tv show mode returns no results if Jellyfin provided no season level IDs`() {
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = true)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            // No getAllSeasons call should be made in whole show mode

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt1111111", season = 1)),
                LibraryType.TV_SHOWS, false
            )

            assertThat(result[MediaLookup(10, 1)]).isNull()
        }

        @Test
        fun `whole tv show mode returns series-level Jellyfin IDs keyed without season`() {
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = true)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            // No getAllSeasons call should be made in whole show mode

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt1111111", season = 1)),
                LibraryType.TV_SHOWS, false
            )

            assertThat(result[MediaLookup(10)]).containsExactly("jf-show-1")
        }

        @Test
        fun `bySeason false returns series-level Jellyfin IDs keyed without season`() {
            // This is the path used by Jellystat/Streamystats when wholeTvShow=true
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            // bySeason=false means no season fetching

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt1111111", season = 2)),
                LibraryType.TV_SHOWS, false
            )

            // bySeason=false: lookup key is MediaLookup(id) without season
            assertThat(result[MediaLookup(10)]).containsExactly("jf-show-1")
        }

        @Test
        fun `multiple items of same show with bySeason false collapse to single lookup key`() {
            val showProviderIds = ProviderIds(Tmdb = "300")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )

            val items = listOf(
                libraryItem(10, tmdbId = 300, season = 1),
                libraryItem(10, tmdbId = 300, season = 2),
                libraryItem(10, tmdbId = 300, season = 3)
            )

            val result = service.getMediaServerIdsForLibrary(items, LibraryType.TV_SHOWS, false)

            // All seasons grouped under MediaLookup(10) - each flatMaps to the same show ID
            val ids = result[MediaLookup(10)]
            assertThat(ids).isNotNull
            assertThat(ids).contains("jf-show-1")
        }
    }

    // ========== Stats service lookup key alignment ==========

    @Nested
    inner class StatsServiceKeyAlignment {

        /**
         * Simulates what Jellystat/Streamystats do: they call getMediaServerIdsForLibrary with
         * bySeason = !wholeTvShow, then look up results using the same key construction.
         * This test verifies the keys produced by getMediaServerIdsForLibrary actually match
         * what the stats services construct.
         */
        @Test
        fun `season-level lookup keys match what stats services construct - wholeTvShow false`() {
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(
                    seasonContent("jf-s1", "Season 1", 1, "jf-show-1"),
                    seasonContent("jf-s2", "Season 2", 2, "jf-show-1")
                ),
                0, 2
            )

            val items = listOf(
                libraryItem(10, imdbId = "tt1111111", season = 1),
                libraryItem(10, imdbId = "tt1111111", season = 2)
            )

            // Stats service calls with bySeason = !wholeTvShow = true
            val statsWholeTvShow = false
            val bySeason = !statsWholeTvShow
            val result = service.getMediaServerIdsForLibrary(items, LibraryType.TV_SHOWS, bySeason)

            // Stats service constructs lookup keys like this:
            for (item in items) {
                val lookupKey = if (!statsWholeTvShow) MediaLookup(item.id, item.season) else MediaLookup(item.id)
                val jellyfinIds = result.getOrDefault(lookupKey, listOf())
                assertThat(jellyfinIds)
                    .describedAs("Season ${item.season} should have Jellyfin IDs via lookup key $lookupKey")
                    .isNotEmpty
            }
        }

        @Test
        fun `show-level lookup keys match what stats services construct - wholeTvShow true`() {
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )

            val items = listOf(
                libraryItem(10, imdbId = "tt1111111", season = 1),
                libraryItem(10, imdbId = "tt1111111", season = 2)
            )

            // Stats service calls with bySeason = !wholeTvShow = false
            val statsWholeTvShow = true
            val bySeason = !statsWholeTvShow
            val result = service.getMediaServerIdsForLibrary(items, LibraryType.TV_SHOWS, bySeason)

            // Stats service constructs lookup keys like this:
            for (item in items) {
                val lookupKey = if (!statsWholeTvShow) MediaLookup(item.id, item.season) else MediaLookup(item.id)
                val jellyfinIds = result.getOrDefault(lookupKey, listOf())
                assertThat(jellyfinIds)
                    .describedAs("Show should have Jellyfin IDs via lookup key $lookupKey")
                    .isNotEmpty
            }
        }
    }

    // ========== Edge Cases ==========

    @Nested
    inner class EdgeCases {

        @Test
        fun `empty library items list returns empty map`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(emptyList(), 0, 0)

            val result = service.getMediaServerIdsForLibrary(emptyList(), LibraryType.MOVIES, false)

            assertThat(result).isEmpty()
        }

        @Test
        fun `empty Jellyfin library returns map with empty value lists`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(emptyList(), 0, 0)

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt1111111")),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).isEmpty()
        }

        @Test
        fun `library item with null provider IDs does not match anything`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", ProviderIds(Imdb = "tt1111111"))),
                0, 1
            )

            // Item has no IMDB, TMDB, or TVDB ID
            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1)),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).isEmpty()
        }

        @Test
        fun `Jellyfin content with null provider IDs does not match`() {
            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", null)),
                0, 1
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt1111111")),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).isEmpty()
        }

        @Test
        fun `Playlists parent folder is excluded from library scan`() {
            val playlistFolder = mockk<MediaFolderItem> {
                every { Id } returns "playlist-folder-id"
                every { Type } returns "ManualPlaylistsFolder"
                every { Name } returns "Playlists"
            }
            every { mediaServerClient.getAllItems() } returns ItemPage(listOf(parentFolder, playlistFolder), 0, 2)

            val service = createService()
            every { mediaServerClient.getAllMovies("parent-folder-id") } returns ItemPage(
                listOf(movieContent("jf-100", ProviderIds(Imdb = "tt1111111"))),
                0, 1
            )
            // getAllMovies should NOT be called for playlist-folder-id

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(1, imdbId = "tt1111111")),
                LibraryType.MOVIES, false
            )

            assertThat(result[MediaLookup(1)]).containsExactly("jf-100")
        }

        @Test
        fun `tv show with specials season zero matched by IndexNumber`() {
            val showProviderIds = ProviderIds(Imdb = "tt1111111")
            val service = createService(wholeTvShow = false)

            every { mediaServerClient.getAllTvShows("parent-folder-id") } returns ItemPage(
                listOf(seriesContent("jf-show-1", showProviderIds)),
                0, 1
            )
            every { mediaServerClient.getAllSeasons("jf-show-1") } returns ItemPage(
                listOf(
                    seasonContent("jf-specials", "Specials", 0, "jf-show-1"),
                    seasonContent("jf-s1", "Season 1", 1, "jf-show-1")
                ),
                0, 2
            )

            val result = service.getMediaServerIdsForLibrary(
                listOf(libraryItem(10, imdbId = "tt1111111", season = 0)),
                LibraryType.TV_SHOWS, true
            )

            assertThat(result[MediaLookup(10, 0)]).containsExactly("jf-specials")
        }
    }
}
