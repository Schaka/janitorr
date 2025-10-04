package com.github.schaka.janitorr.metrics

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetricsServiceTest {

    private lateinit var metricsService: MetricsService

    @BeforeEach
    fun setUp() {
        metricsService = MetricsService()
    }

    @Test
    fun `should record cleanup event`() {
        metricsService.recordCleanup("movies", 5, 1_000_000_000L)

        val summary = metricsService.getSummary()
        
        assertEquals(5, summary.totalFilesDeleted)
        assertEquals(1_000_000_000L, summary.totalSpaceFreed)
    }

    @Test
    fun `should track media type counts`() {
        metricsService.recordCleanup("movies", 3, 500_000_000L)
        metricsService.recordCleanup("shows", 2, 300_000_000L)

        val distribution = metricsService.getMediaTypeDistribution()
        
        assertTrue(distribution.containsKey("movies"))
        assertTrue(distribution.containsKey("shows"))
        assertEquals(3, distribution["movies"])
        assertEquals(2, distribution["shows"])
    }

    @Test
    fun `should maintain cleanup history`() {
        metricsService.recordCleanup("episodes", 10, 2_000_000_000L)

        val history = metricsService.getCleanupHistory(10)
        
        assertEquals(1, history.size)
        assertEquals("episodes", history[0].type)
        assertEquals(10, history[0].filesDeleted)
        assertEquals(2_000_000_000L, history[0].spaceFreed)
    }

    @Test
    fun `should limit cleanup history size`() {
        // Record more than MAX_HISTORY_SIZE events
        repeat(1500) {
            metricsService.recordCleanup("test", 1, 100_000L)
        }

        val history = metricsService.getCleanupHistory(2000)
        
        // Should not exceed MAX_HISTORY_SIZE (1000)
        assertTrue(history.size <= 1000)
    }

    @Test
    fun `should calculate summary correctly`() {
        val service = MetricsService()
        service.recordCleanup("movies", 5, 5_000_000_000L)
        service.recordCleanup("shows", 3, 3_000_000_000L)

        val summary = service.getSummary()

        assertEquals(8, summary.totalFilesDeleted)
        assertEquals(8_000_000_000L, summary.totalSpaceFreed)
    }
}
