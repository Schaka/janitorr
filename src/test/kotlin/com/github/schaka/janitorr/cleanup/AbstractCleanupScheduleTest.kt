package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.EpisodeDeletion
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.config.MediaDeletion
import com.github.schaka.janitorr.config.TagDeletion
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.stats.StatsService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class AbstractCleanupScheduleTest {

    @MockK(relaxed = true)
    lateinit var mediaServerService: AbstractMediaServerService

    @MockK(relaxed = true)
    lateinit var jellyseerrService: JellyseerrService

    @MockK(relaxed = true)
    lateinit var statsService: StatsService

    @MockK(relaxed = true)
    lateinit var sonarrService: ServarrService

    @MockK(relaxed = true)
    lateinit var radarrService: ServarrService

    @Test
    fun scheduleDeleteUpdatesLeavingSoonWithoutDeleting() {
        val now = LocalDateTime.now()
        val olderItem = LibraryItem(
            id = 1,
            importedDate = now.minusDays(11),
            originalPath = "/data/media/movies/Movie (2024)/Movie.mkv",
            libraryPath = "/data/media/movies/Movie (2024)/Movie.mkv",
            parentPath = "/data/media/movies/Movie (2024)",
            rootFolderPath = "/data/media/movies",
            filePath = "/data/media/movies/Movie (2024)/Movie.mkv"
        )
        val newerItem = LibraryItem(
            id = 2,
            importedDate = now.minusDays(9),
            originalPath = "/data/media/movies/Movie 2 (2024)/Movie2.mkv",
            libraryPath = "/data/media/movies/Movie 2 (2024)/Movie2.mkv",
            parentPath = "/data/media/movies/Movie 2 (2024)",
            rootFolderPath = "/data/media/movies",
            filePath = "/data/media/movies/Movie 2 (2024)/Movie2.mkv"
        )

        every { radarrService.getEntries() } returns listOf(olderItem, newerItem)
        every { mediaServerService.filterOutFavorites(any(), any()) } returns listOf(olderItem, newerItem)

        val schedule = buildSchedule(
            shouldDelete = false,
            leavingSoonDuration = Duration.ofDays(10),
            leavingSoonWindow = Duration.ofDays(2)
        )

        schedule.exposeScheduleDelete(LibraryType.MOVIES, Duration.ofDays(30))

        verify(exactly = 1) {
            mediaServerService.updateLeavingSoon(
                CleanupType.MEDIA,
                LibraryType.MOVIES,
                match { it.map { item -> item.id } == listOf(olderItem.id) },
                false
            )
        }
        verify(exactly = 0) { radarrService.removeEntries(any()) }
        verify(exactly = 0) { mediaServerService.cleanupMovies(any()) }
        verify(exactly = 0) { jellyseerrService.cleanupRequests(any()) }
    }

    @Test
    fun scheduleDeleteSkipsWorkWhenNoDeletionAndNoLeavingSoon() {
        val schedule = buildSchedule(
            shouldDelete = false,
            leavingSoonDuration = Duration.ZERO,
            leavingSoonWindow = Duration.ofDays(2)
        )

        schedule.exposeScheduleDelete(LibraryType.MOVIES, Duration.ofDays(30))

        verify(exactly = 0) { radarrService.getEntries() }
        verify(exactly = 0) { mediaServerService.updateLeavingSoon(any(), any(), any(), any()) }
        verify(exactly = 0) { statsService.populateWatchHistory(any(), any()) }
    }

    private fun buildSchedule(
        shouldDelete: Boolean,
        leavingSoonDuration: Duration,
        leavingSoonWindow: Duration
    ): TestCleanupSchedule {
        val fileSystemProperties = FileSystemProperties(
            access = false,
            leavingSoonDir = "/data/media/leaving-soon",
            mediaServerLeavingSoonDir = "/data/media/leaving-soon",
            validateSeeding = true,
            fromScratch = true,
            freeSpaceCheckDir = "/"
        )

        val applicationProperties = ApplicationProperties(
            mediaDeletion = MediaDeletion(),
            tagBasedDeletion = TagDeletion(),
            episodeDeletion = EpisodeDeletion(),
            leavingSoon = leavingSoonWindow
        )

        return TestCleanupSchedule(
            mediaServerService,
            jellyseerrService,
            statsService,
            fileSystemProperties,
            applicationProperties,
            sonarrService,
            radarrService,
            shouldDelete,
            leavingSoonDuration
        )
    }

    private class TestCleanupSchedule(
        mediaServerService: AbstractMediaServerService,
        jellyseerrService: JellyseerrService,
        statsService: StatsService,
        fileSystemProperties: FileSystemProperties,
        applicationProperties: ApplicationProperties,
        sonarrService: ServarrService,
        radarrService: ServarrService,
        private val shouldDelete: Boolean,
        private val leavingSoonDuration: Duration
    ) : AbstractCleanupSchedule(
        CleanupType.MEDIA,
        mediaServerService,
        jellyseerrService,
        statsService,
        fileSystemProperties,
        applicationProperties,
        sonarrService,
        radarrService
    ) {
        override fun needToDelete(type: LibraryType): Boolean = shouldDelete

        override fun determineLeavingSoonDuration(type: LibraryType): Duration = leavingSoonDuration

        fun exposeScheduleDelete(libraryType: LibraryType, expiration: Duration?) {
            scheduleDelete(libraryType, expiration)
        }
    }
}
