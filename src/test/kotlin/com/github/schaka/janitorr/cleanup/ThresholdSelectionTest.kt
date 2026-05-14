package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.*
import com.github.schaka.janitorr.seerr.SeerrService
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.stats.StatsService
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.temporal.ChronoUnit.FOREVER

@ExtendWith(MockKExtension::class)
class ThresholdSelectionTest {

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

    private val threeThresholds = mapOf(
        10 to Duration.ofDays(180),
        20 to Duration.ofDays(365),
        50 to Duration.ofDays(730)
    )

    @Test
    fun `picks lowest threshold above free space`() {
        // 19.76% free - should pick 20% (365d), not 10% or 50%
        val duration = buildSchedule().exposeThresholdSelection(threeThresholds, 19.758291652159638)
        assertThat(duration).isEqualTo(Duration.ofDays(365))
    }

    @Test
    fun `picks 10% threshold when space below 10%`() {
        val duration = buildSchedule().exposeThresholdSelection(threeThresholds, 9.5)
        assertThat(duration).isEqualTo(Duration.ofDays(180))
    }

    @Test
    fun `picks 50% threshold when space between 20% and 50%`() {
        val duration = buildSchedule().exposeThresholdSelection(threeThresholds, 35.0)
        assertThat(duration).isEqualTo(Duration.ofDays(730))
    }

    @Test
    fun `returns FOREVER when free space above all thresholds`() {
        val duration = buildSchedule().exposeThresholdSelection(threeThresholds, 55.0)
        assertThat(duration).isEqualTo(FOREVER.duration)
    }

    @Test
    fun `exactly at 10% does not trigger 10% threshold`() {
        // 10.0 < 10 is false - picks next threshold (20% -> 365d)
        val duration = buildSchedule().exposeThresholdSelection(threeThresholds, 10.0)
        assertThat(duration).isEqualTo(Duration.ofDays(365))
    }

    @Test
    fun `exactly at 20% does not trigger 20% threshold`() {
        // 20.0 < 20 is false - picks next threshold (50% -> 730d)
        val duration = buildSchedule().exposeThresholdSelection(threeThresholds, 20.0)
        assertThat(duration).isEqualTo(Duration.ofDays(730))
    }

    @Test
    fun `near-zero free space picks lowest threshold`() {
        val duration = buildSchedule().exposeThresholdSelection(threeThresholds, 0.1)
        assertThat(duration).isEqualTo(Duration.ofDays(180))
    }

    private fun buildSchedule(): TestMediaCleanupSchedule {
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
        return TestMediaCleanupSchedule(mediaServerService, seerrService, statsService, fileSystemProperties, applicationProperties, sonarrService, radarrService)
    }

    private class TestMediaCleanupSchedule(
        mediaServerService: AbstractMediaServerService,
        seerrService: SeerrService,
        statsService: StatsService,
        fileSystemProperties: FileSystemProperties,
        applicationProperties: ApplicationProperties,
        sonarrService: ServarrService,
        radarrService: ServarrService
    ) : MediaCleanupSchedule(
        mediaServerService,
        seerrService,
        statsService,
        fileSystemProperties,
        applicationProperties,
        sonarrService,
        radarrService
    ) {
        fun exposeThresholdSelection(conditions: Map<Int, Duration>, freeSpacePercentage: Double): Duration {
            return determineDeletionDuration(conditions, freeSpacePercentage)
        }
    }
}
