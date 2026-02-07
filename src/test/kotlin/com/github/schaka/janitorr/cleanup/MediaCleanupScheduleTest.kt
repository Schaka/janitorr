package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.EpisodeDeletion
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.config.MediaDeletion
import com.github.schaka.janitorr.config.TagDeletion
import com.github.schaka.janitorr.jellyseerr.JellyseerrService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.stats.StatsService
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.time.Duration
import kotlin.math.floor
import kotlin.math.max

@ExtendWith(MockKExtension::class)
class MediaCleanupScheduleTest {

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
    fun determineLeavingSoonDurationUsesOffsetThreshold() {
        val fileSystemProperties = FileSystemProperties(
            access = true,
            leavingSoonDir = "/data/media/leaving-soon",
            mediaServerLeavingSoonDir = "/data/media/leaving-soon",
            validateSeeding = true,
            fromScratch = true,
            freeSpaceCheckDir = "/"
        )

        val freeSpacePercentage = freeSpacePercent(fileSystemProperties.freeSpaceCheckDir)
        val deleteThreshold = max(6, floor(freeSpacePercentage).toInt() + 1)
        val offset = 5
        val leavingSoonThreshold = deleteThreshold - offset

        val mediaDeletion = MediaDeletion(
            enabled = true,
            movieExpiration = mapOf(
                leavingSoonThreshold to Duration.ofDays(10),
                deleteThreshold to Duration.ofDays(20)
            ),
            seasonExpiration = mapOf(
                leavingSoonThreshold to Duration.ofDays(10)
            )
        )

        val applicationProperties = ApplicationProperties(
            mediaDeletion = mediaDeletion,
            tagBasedDeletion = TagDeletion(),
            episodeDeletion = EpisodeDeletion(),
            leavingSoonThresholdOffsetPercent = offset
        )

        val schedule = TestMediaCleanupSchedule(
            mediaServerService,
            jellyseerrService,
            statsService,
            fileSystemProperties,
            applicationProperties,
            sonarrService,
            radarrService
        )

        val duration = schedule.exposeDetermineLeavingSoonDuration(LibraryType.MOVIES)

        assertThat(duration).isEqualTo(Duration.ofDays(10))
    }

    @Test
    fun determineLeavingSoonDurationIsZeroWhenOffsetIsDisabled() {
        val fileSystemProperties = FileSystemProperties(
            access = true,
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
            leavingSoonThresholdOffsetPercent = 0
        )

        val schedule = TestMediaCleanupSchedule(
            mediaServerService,
            jellyseerrService,
            statsService,
            fileSystemProperties,
            applicationProperties,
            sonarrService,
            radarrService
        )

        val duration = schedule.exposeDetermineLeavingSoonDuration(LibraryType.MOVIES)

        assertThat(duration).isEqualTo(Duration.ZERO)
    }

    @Test
    fun determineLeavingSoonDurationIsZeroWithoutFilesystemAccess() {
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
            leavingSoonThresholdOffsetPercent = 5
        )

        val schedule = TestMediaCleanupSchedule(
            mediaServerService,
            jellyseerrService,
            statsService,
            fileSystemProperties,
            applicationProperties,
            sonarrService,
            radarrService
        )

        val duration = schedule.exposeDetermineLeavingSoonDuration(LibraryType.MOVIES)

        assertThat(duration).isEqualTo(Duration.ZERO)
    }

    private fun freeSpacePercent(dir: String): Double {
        val filesystem = File(dir)
        return (filesystem.usableSpace.toDouble() / filesystem.totalSpace.toDouble()) * 100
    }

    private class TestMediaCleanupSchedule(
        mediaServerService: AbstractMediaServerService,
        jellyseerrService: JellyseerrService,
        statsService: StatsService,
        fileSystemProperties: FileSystemProperties,
        applicationProperties: ApplicationProperties,
        sonarrService: ServarrService,
        radarrService: ServarrService
    ) : MediaCleanupSchedule(
        mediaServerService,
        jellyseerrService,
        statsService,
        fileSystemProperties,
        applicationProperties,
        sonarrService,
        radarrService
    ) {
        fun exposeDetermineLeavingSoonDuration(type: LibraryType): Duration {
            return determineLeavingSoonDuration(type)
        }
    }
}
