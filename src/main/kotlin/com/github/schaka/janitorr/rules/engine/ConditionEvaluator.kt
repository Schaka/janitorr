package com.github.schaka.janitorr.rules.engine

import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.rules.model.*
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Evaluates conditions against media items
 */
@Component
class ConditionEvaluator(
    private val fileSystemProperties: FileSystemProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    fun evaluate(condition: Condition, mediaItem: LibraryItem): Boolean {
        return try {
            when (condition) {
                is AgeCondition -> evaluateAge(condition, mediaItem)
                is SizeCondition -> evaluateSize(condition, mediaItem)
                is RatingCondition -> evaluateRating(condition, mediaItem)
                is GenreCondition -> evaluateGenre(condition, mediaItem)
                is DiskUsageCondition -> evaluateDiskUsage(condition)
                is PlaysCondition -> evaluatePlays(condition, mediaItem)
                is ImdbRatingCondition -> evaluateImdbRating(condition, mediaItem)
                is ReleaseYearCondition -> evaluateReleaseYear(condition, mediaItem)
                is TagCondition -> evaluateTag(condition, mediaItem)
                else -> {
                    log.warn("Unknown condition type: {}", condition.type)
                    false
                }
            }
        } catch (e: Exception) {
            log.error("Error evaluating condition {} for media {}", condition.type, mediaItem.libraryPath, e)
            false
        }
    }

    private fun evaluateAge(condition: AgeCondition, mediaItem: LibraryItem): Boolean {
        val ageInDays = ChronoUnit.DAYS.between(mediaItem.importedDate, LocalDateTime.now())
        return compare(ageInDays.toInt(), condition.days, condition.operator)
    }

    private fun evaluateSize(condition: SizeCondition, mediaItem: LibraryItem): Boolean {
        val file = File(mediaItem.filePath)
        if (!file.exists()) {
            log.debug("File does not exist: {}", mediaItem.filePath)
            return false
        }
        
        val sizeInGB = file.length() / (1024.0 * 1024.0 * 1024.0)
        return compare(sizeInGB, condition.sizeInGB, condition.operator)
    }

    private fun evaluateRating(condition: RatingCondition, mediaItem: LibraryItem): Boolean {
        // Rating would typically come from external metadata
        // For now, we return false as this needs integration with media server
        log.debug("Rating evaluation not yet implemented for {}", mediaItem.libraryPath)
        return false
    }

    private fun evaluateGenre(condition: GenreCondition, mediaItem: LibraryItem): Boolean {
        // Genre would typically come from external metadata
        // For now, we return false as this needs integration with media server
        log.debug("Genre evaluation not yet implemented for {}", mediaItem.libraryPath)
        return false
    }

    private fun evaluateDiskUsage(condition: DiskUsageCondition): Boolean {
        val filesystem = File(fileSystemProperties.freeSpaceCheckDir)
        val usedPercentage = (1 - (filesystem.usableSpace.toDouble() / filesystem.totalSpace.toDouble())) * 100
        return compare(usedPercentage, condition.percentage, condition.operator)
    }

    private fun evaluatePlays(condition: PlaysCondition, mediaItem: LibraryItem): Boolean {
        // Play count would come from media server or stats service
        // For now, we check if lastSeen is null (never played) for zero plays
        if (condition.plays == 0 && condition.operator == ComparisonOperator.EQUALS) {
            return mediaItem.lastSeen == null
        }
        log.debug("Plays evaluation with count > 0 not yet fully implemented for {}", mediaItem.libraryPath)
        return false
    }

    private fun evaluateImdbRating(condition: ImdbRatingCondition, mediaItem: LibraryItem): Boolean {
        // IMDB rating would come from external API
        // For now, we return false as this needs external integration
        log.debug("IMDB rating evaluation not yet implemented for {}", mediaItem.libraryPath)
        return false
    }

    private fun evaluateReleaseYear(condition: ReleaseYearCondition, mediaItem: LibraryItem): Boolean {
        // Release year would come from metadata
        // For now, we return false as this needs integration with media server
        log.debug("Release year evaluation not yet implemented for {}", mediaItem.libraryPath)
        return false
    }

    private fun evaluateTag(condition: TagCondition, mediaItem: LibraryItem): Boolean {
        return when (condition.operator) {
            ComparisonOperator.CONTAINS, ComparisonOperator.EQUALS -> 
                mediaItem.tags.contains(condition.tag)
            ComparisonOperator.NOT_CONTAINS, ComparisonOperator.NOT_EQUALS -> 
                !mediaItem.tags.contains(condition.tag)
            else -> {
                log.warn("Unsupported operator {} for tag condition", condition.operator)
                false
            }
        }
    }

    private fun <T : Comparable<T>> compare(actual: T, expected: T, operator: ComparisonOperator): Boolean {
        return when (operator) {
            ComparisonOperator.EQUALS -> actual == expected
            ComparisonOperator.NOT_EQUALS -> actual != expected
            ComparisonOperator.GREATER_THAN -> actual > expected
            ComparisonOperator.LESS_THAN -> actual < expected
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> actual >= expected
            ComparisonOperator.LESS_THAN_OR_EQUAL -> actual <= expected
            else -> {
                log.warn("Unsupported operator {} for comparison", operator)
                false
            }
        }
    }
}
