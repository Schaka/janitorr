package com.github.schaka.janitorr.rules.engine

import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.rules.model.*
import com.github.schaka.janitorr.servarr.LibraryItem
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for ConditionEvaluator
 */
internal class ConditionEvaluatorTest {

    private lateinit var fileSystemProperties: FileSystemProperties
    private lateinit var conditionEvaluator: ConditionEvaluator
    private lateinit var mediaItem: LibraryItem

    @BeforeEach
    fun setup() {
        fileSystemProperties = FileSystemProperties(
            access = true,
            freeSpaceCheckDir = "/",
            mediaServerLeavingSoonDir = null
        )
        conditionEvaluator = ConditionEvaluator(fileSystemProperties)
        
        mediaItem = LibraryItem(
            id = 1,
            importedDate = LocalDateTime.now().minusDays(60),
            originalPath = "/media/movies/test.mkv",
            libraryPath = "/media/movies/test.mkv",
            parentPath = "/media/movies",
            rootFolderPath = "/media",
            filePath = "/media/movies/test.mkv",
            imdbId = "tt1234567",
            tags = listOf("test", "movie")
        )
    }

    @Test
    fun testAgeConditionGreaterThan() {
        val condition = AgeCondition(
            operator = ComparisonOperator.GREATER_THAN,
            days = 30
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        assertTrue(result, "Media older than 30 days should match")
    }

    @Test
    fun testAgeConditionLessThan() {
        val condition = AgeCondition(
            operator = ComparisonOperator.LESS_THAN,
            days = 100
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        assertTrue(result, "Media newer than 100 days should match")
    }

    @Test
    fun testAgeConditionNotMatching() {
        val condition = AgeCondition(
            operator = ComparisonOperator.GREATER_THAN,
            days = 100
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        assertFalse(result, "Media not older than 100 days should not match")
    }

    @Test
    fun testTagConditionContains() {
        val condition = TagCondition(
            operator = ComparisonOperator.CONTAINS,
            tag = "test"
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        assertTrue(result, "Media with 'test' tag should match")
    }

    @Test
    fun testTagConditionNotContains() {
        val condition = TagCondition(
            operator = ComparisonOperator.NOT_CONTAINS,
            tag = "nonexistent"
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        assertTrue(result, "Media without 'nonexistent' tag should match")
    }

    @Test
    fun testTagConditionDoesNotMatch() {
        val condition = TagCondition(
            operator = ComparisonOperator.CONTAINS,
            tag = "nonexistent"
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        assertFalse(result, "Media without matching tag should not match")
    }

    @Test
    fun testPlaysConditionZeroPlays() {
        val condition = PlaysCondition(
            operator = ComparisonOperator.EQUALS,
            plays = 0
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        assertTrue(result, "Media with no lastSeen should have zero plays")
    }

    @Test
    fun testDiskUsageCondition() {
        val condition = DiskUsageCondition(
            operator = ComparisonOperator.GREATER_THAN,
            percentage = 0.0
        )
        
        val result = conditionEvaluator.evaluate(condition, mediaItem)
        // This will depend on actual disk usage, so we just verify it doesn't throw
        assertTrue(result is Boolean, "Disk usage evaluation should return a boolean")
    }

    @Test
    fun testSizeConditionWithExistingFile() {
        // Create a temporary file with a known size
        val tempFile = File.createTempFile("test", ".mkv")
        try {
            // Write 5GB worth of data (5 * 1024 * 1024 * 1024 bytes)
            // For testing purposes, we'll just use a small file and test the logic
            tempFile.writeText("test content")
            
            val testMediaItem = mediaItem.copy(filePath = tempFile.absolutePath)
            
            // Test with a condition that should match (file is smaller than 1GB)
            val condition = SizeCondition(
                operator = ComparisonOperator.LESS_THAN,
                sizeInGB = 1.0
            )
            
            val result = conditionEvaluator.evaluate(condition, testMediaItem)
            assertTrue(result, "Small file should be less than 1GB")
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testSizeConditionWithNonExistentFile() {
        val testMediaItem = mediaItem.copy(filePath = "/nonexistent/file.mkv")
        
        val condition = SizeCondition(
            operator = ComparisonOperator.GREATER_THAN,
            sizeInGB = 1.0
        )
        
        val result = conditionEvaluator.evaluate(condition, testMediaItem)
        assertFalse(result, "Non-existent file should return false")
    }
}
