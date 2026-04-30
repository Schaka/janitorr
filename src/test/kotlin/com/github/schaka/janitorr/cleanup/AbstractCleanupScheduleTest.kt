package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.*
import com.github.schaka.janitorr.seerr.SeerrService
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
import java.time.temporal.ChronoUnit.FOREVER

@ExtendWith(MockKExtension::class)
class AbstractCleanupScheduleTest {

    @MockK(relaxed = true)
    lateinit var mediaServerService: AbstractMediaServerService

    @MockK(relaxed = true)
    lateinit var seerrService: SeerrService

    @MockK(relaxed = true)
    lateinit var statsService: StatsService

    @MockK(relaxed = true)
    lateinit var sonarrService: ServarrService

    @MockK(relaxed = true)
    lateinit var radarrService: ServarrService

    @Test
    fun `Leaving Soon runs independently of deletion`() {
        val now = LocalDateTime.now()
        val olderItem = LibraryItem(
            id = 1,
            importedDate = now.minusDays(25),
            originalPath = "/data/media/movies/Movie (2024)/Movie.mkv",
            libraryPath = "/data/media/movies/Movie (2024)/Movie.mkv",
            parentPath = "/data/media/movies/Movie (2024)",
            rootFolderPath = "/data/media/movies",
            filePath = "/data/media/movies/Movie (2024)/Movie.mkv"
        )
        val tooNewForLeavingSoon = LibraryItem(
            id = 2,
            importedDate = now.minusDays(19),
            originalPath = "/data/media/movies/Movie 2 (2024)/Movie2.mkv",
            libraryPath = "/data/media/movies/Movie 2 (2024)/Movie2.mkv",
            parentPath = "/data/media/movies/Movie 2 (2024)",
            rootFolderPath = "/data/media/movies",
            filePath = "/data/media/movies/Movie 2 (2024)/Movie2.mkv"
        )

        every { radarrService.getEntries() } returns listOf(olderItem, tooNewForLeavingSoon)
        every { mediaServerService.filterOutFavorites(any(), any()) } answers { firstArg() }

        val schedule = buildSchedule(
            shouldDelete = false,
            leavingSoonDeletionDuration = Duration.ofDays(30),
            leavingSoonWindow = Duration.ofDays(10)
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
        verify(exactly = 0) { seerrService.cleanupRequests(any()) }
    }

    @Test
    fun `Deletion is skipped when no deletion and leaving soon are required`() {
        val schedule = buildSchedule(
            shouldDelete = false,
            leavingSoonDeletionDuration = FOREVER.duration,
            leavingSoonWindow = Duration.ofDays(2)
        )

        schedule.exposeScheduleDelete(LibraryType.MOVIES, Duration.ofDays(30))

        verify(exactly = 0) { radarrService.getEntries() }
        verify(exactly = 0) { mediaServerService.updateLeavingSoon(any(), any(), any(), any()) }
        verify(exactly = 0) { statsService.populateWatchHistory(any(), any()) }
    }

    private fun buildSchedule(
        shouldDelete: Boolean,
        leavingSoonDeletionDuration: Duration,
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
            seerrService,
            statsService,
            fileSystemProperties,
            applicationProperties,
            sonarrService,
            radarrService,
            shouldDelete,
            leavingSoonDeletionDuration
        )
    }

    private class TestCleanupSchedule(
        mediaServerService: AbstractMediaServerService,
        seerrService: SeerrService,
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
        seerrService,
        statsService,
        fileSystemProperties,
        applicationProperties,
        sonarrService,
        radarrService
    ) {
        override fun needToDelete(type: LibraryType): Boolean = shouldDelete

        fun exposeScheduleDelete(libraryType: LibraryType, expiration: Duration) {
            scheduleDelete(libraryType, expiration, leavingSoonDuration)
        }
    }
}
