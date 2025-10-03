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
        
        // Account for sample data initialized in the service
        assertTrue(summary.totalFilesDeleted >= 5)
        assertTrue(summary.totalSpaceFreed >= 1_000_000_000L)
    }

    @Test
    fun `should track media type counts`() {
        metricsService.recordCleanup("movies", 3, 500_000_000L)
        metricsService.recordCleanup("shows", 2, 300_000_000L)

        val distribution = metricsService.getMediaTypeDistribution()
        
        assertTrue(distribution.containsKey("movies"))
        assertTrue(distribution.containsKey("shows"))
        assertTrue(distribution["movies"]!! >= 3)
        assertTrue(distribution["shows"]!! >= 2)
    }

    @Test
    fun `should maintain cleanup history`() {
        metricsService.recordCleanup("episodes", 10, 2_000_000_000L)

        val history = metricsService.getCleanupHistory(10)
        
        assertTrue(history.isNotEmpty())
        assertTrue(history.any { it.type == "episodes" })
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

        // Check that totals include our recorded events (plus sample data)
        assertTrue(summary.totalFilesDeleted >= 8)
        assertTrue(summary.totalSpaceFreed >= 8_000_000_000L)
    }
}
