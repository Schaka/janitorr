package com.github.schaka.janitorr.rules.model

/**
 * Base interface for all action types
 */
sealed interface Action {
    val type: ActionType
}

/**
 * Types of actions that can be executed
 */
enum class ActionType {
    DELETE_FILE,
    MOVE_TO,
    ADD_TAG,
    REMOVE_TAG,
    NOTIFY_DISCORD,
    NOTIFY_EMAIL,
    LOG_ACTION,
    REMOVE_FROM_SONARR,
    REMOVE_FROM_RADARR,
    ADD_TO_EXCLUSION
}

/**
 * Action to delete media file
 */
data class DeleteFileAction(
    val removeFromMediaServer: Boolean = true
) : Action {
    override val type = ActionType.DELETE_FILE
}

/**
 * Action to move media to another location
 */
data class MoveToAction(
    val destinationPath: String
) : Action {
    override val type = ActionType.MOVE_TO
}

/**
 * Action to add a tag to media
 */
data class AddTagAction(
    val tag: String
) : Action {
    override val type = ActionType.ADD_TAG
}

/**
 * Action to remove a tag from media
 */
data class RemoveTagAction(
    val tag: String
) : Action {
    override val type = ActionType.REMOVE_TAG
}

/**
 * Action to send Discord notification
 */
data class NotifyDiscordAction(
    val message: String,
    val webhookUrl: String? = null
) : Action {
    override val type = ActionType.NOTIFY_DISCORD
}

/**
 * Action to log the action
 */
data class LogAction(
    val level: LogLevel = LogLevel.INFO,
    val message: String
) : Action {
    override val type = ActionType.LOG_ACTION
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * Action to remove from Sonarr
 */
data class RemoveFromSonarrAction(
    val deleteFiles: Boolean = true
) : Action {
    override val type = ActionType.REMOVE_FROM_SONARR
}

/**
 * Action to remove from Radarr
 */
data class RemoveFromRadarrAction(
    val deleteFiles: Boolean = true
) : Action {
    override val type = ActionType.REMOVE_FROM_RADARR
}

/**
 * Action to add to exclusion list
 */
data class AddToExclusionAction(
    val reason: String? = null
) : Action {
    override val type = ActionType.ADD_TO_EXCLUSION
}
