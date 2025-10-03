package com.github.schaka.janitorr.rules.model

/**
 * Base interface for all condition types
 */
sealed interface Condition {
    val type: ConditionType
    val operator: ComparisonOperator
}

/**
 * Types of conditions that can be evaluated
 */
enum class ConditionType {
    // Media properties
    AGE,
    SIZE,
    RATING,
    GENRE,
    QUALITY,
    PLAYS,
    FILE_FORMAT,
    RELEASE_YEAR,
    
    // System conditions
    DISK_USAGE,
    TIME_OF_DAY,
    DAY_OF_WEEK,
    
    // External data
    IMDB_RATING,
    SERIES_STATUS,
    TAG
}

/**
 * Comparison operators for condition evaluation
 */
enum class ComparisonOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN_OR_EQUAL,
    CONTAINS,
    NOT_CONTAINS,
    IN,
    NOT_IN,
    BETWEEN
}

/**
 * Condition based on media age
 */
data class AgeCondition(
    override val operator: ComparisonOperator,
    val days: Int
) : Condition {
    override val type = ConditionType.AGE
}

/**
 * Condition based on file size
 */
data class SizeCondition(
    override val operator: ComparisonOperator,
    val sizeInGB: Double
) : Condition {
    override val type = ConditionType.SIZE
}

/**
 * Condition based on rating/score
 */
data class RatingCondition(
    override val operator: ComparisonOperator,
    val rating: Double
) : Condition {
    override val type = ConditionType.RATING
}

/**
 * Condition based on genre
 */
data class GenreCondition(
    override val operator: ComparisonOperator,
    val genre: String
) : Condition {
    override val type = ConditionType.GENRE
}

/**
 * Condition based on disk usage percentage
 */
data class DiskUsageCondition(
    override val operator: ComparisonOperator,
    val percentage: Double
) : Condition {
    override val type = ConditionType.DISK_USAGE
}

/**
 * Condition based on number of plays
 */
data class PlaysCondition(
    override val operator: ComparisonOperator,
    val plays: Int,
    val daysRange: Int? = null
) : Condition {
    override val type = ConditionType.PLAYS
}

/**
 * Condition based on IMDB rating
 */
data class ImdbRatingCondition(
    override val operator: ComparisonOperator,
    val rating: Double
) : Condition {
    override val type = ConditionType.IMDB_RATING
}

/**
 * Condition based on release year
 */
data class ReleaseYearCondition(
    override val operator: ComparisonOperator,
    val year: Int
) : Condition {
    override val type = ConditionType.RELEASE_YEAR
}

/**
 * Condition based on tags
 */
data class TagCondition(
    override val operator: ComparisonOperator,
    val tag: String
) : Condition {
    override val type = ConditionType.TAG
}
