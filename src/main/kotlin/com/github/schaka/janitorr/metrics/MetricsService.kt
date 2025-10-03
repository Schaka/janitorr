package com.github.schaka.janitorr.metrics

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Service for collecting and storing cleanup metrics.
 * Stores metrics in-memory for dashboard analytics.
 */
@Service
class MetricsService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private const val MAX_HISTORY_SIZE = 1000
    }

    private val totalFilesDeleted = AtomicLong(0)
    private val totalSpaceFreed = AtomicLong(0)
    private val mediaTypeCounters = ConcurrentHashMap<String, AtomicLong>()
    private val cleanupHistory = mutableListOf<CleanupEvent>()
    private val serviceLock = Any()

    /**
     * Record a cleanup event
     */
    fun recordCleanup(type: String, filesDeleted: Int, spaceFreed: Long) {
        totalFilesDeleted.addAndGet(filesDeleted.toLong())
        totalSpaceFreed.addAndGet(spaceFreed)
        
        mediaTypeCounters.computeIfAbsent(type) { AtomicLong(0) }
            .addAndGet(filesDeleted.toLong())

        synchronized(serviceLock) {
            cleanupHistory.add(CleanupEvent(
                timestamp = LocalDateTime.now(),
                type = type,
                filesDeleted = filesDeleted,
                spaceFreed = spaceFreed
            ))
            
            // Keep only recent history
            if (cleanupHistory.size > MAX_HISTORY_SIZE) {
                cleanupHistory.removeAt(0)
            }
        }

        log.debug("Recorded cleanup: type=$type, files=$filesDeleted, space=$spaceFreed")
    }

    /**
     * Get summary statistics
     */
    fun getSummary(): MetricsSummary {
        return MetricsSummary(
            totalFilesDeleted = totalFilesDeleted.get(),
            totalSpaceFreed = totalSpaceFreed.get(),
            mediaTypeCounts = mediaTypeCounters.mapValues { it.value.get() }
        )
    }

    /**
     * Get cleanup history for charts
     */
    fun getCleanupHistory(limit: Int = 100): List<CleanupEvent> {
        synchronized(serviceLock) {
            return cleanupHistory.takeLast(limit).toList()
        }
    }

    /**
     * Get media type distribution
     */
    fun getMediaTypeDistribution(): Map<String, Long> {
        return mediaTypeCounters.mapValues { it.value.get() }
    }
}

data class CleanupEvent(
    val timestamp: LocalDateTime,
    val type: String,
    val filesDeleted: Int,
    val spaceFreed: Long
)

data class MetricsSummary(
    val totalFilesDeleted: Long,
    val totalSpaceFreed: Long,
    val mediaTypeCounts: Map<String, Long>
)
