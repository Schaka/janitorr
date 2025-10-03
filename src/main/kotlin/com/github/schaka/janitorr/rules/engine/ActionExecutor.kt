package com.github.schaka.janitorr.rules.engine

import com.github.schaka.janitorr.rules.model.*
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Executes actions on media items
 */
@Component
class ActionExecutor {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    fun execute(action: Action, mediaItem: LibraryItem) {
        when (action) {
            is DeleteFileAction -> executeDelete(action, mediaItem)
            is MoveToAction -> executeMove(action, mediaItem)
            is AddTagAction -> executeAddTag(action, mediaItem)
            is RemoveTagAction -> executeRemoveTag(action, mediaItem)
            is NotifyDiscordAction -> executeNotifyDiscord(action, mediaItem)
            is LogAction -> executeLog(action, mediaItem)
            is RemoveFromSonarrAction -> executeRemoveFromSonarr(action, mediaItem)
            is RemoveFromRadarrAction -> executeRemoveFromRadarr(action, mediaItem)
            is AddToExclusionAction -> executeAddToExclusion(action, mediaItem)
            else -> {
                log.warn("Unknown action type: {}", action.type)
            }
        }
    }

    private fun executeDelete(action: DeleteFileAction, mediaItem: LibraryItem) {
        log.info("Executing DELETE action on: {}", mediaItem.libraryPath)
        // Actual deletion would be handled by existing cleanup services
        // This is a placeholder for integration with existing deletion logic
        log.warn("DELETE action execution requires integration with existing cleanup services")
    }

    private fun executeMove(action: MoveToAction, mediaItem: LibraryItem) {
        log.info("Executing MOVE action to {} on: {}", action.destinationPath, mediaItem.libraryPath)
        log.warn("MOVE action execution not yet implemented")
    }

    private fun executeAddTag(action: AddTagAction, mediaItem: LibraryItem) {
        log.info("Executing ADD_TAG action ({}) on: {}", action.tag, mediaItem.libraryPath)
        log.warn("ADD_TAG action execution requires integration with *arr services")
    }

    private fun executeRemoveTag(action: RemoveTagAction, mediaItem: LibraryItem) {
        log.info("Executing REMOVE_TAG action ({}) on: {}", action.tag, mediaItem.libraryPath)
        log.warn("REMOVE_TAG action execution requires integration with *arr services")
    }

    private fun executeNotifyDiscord(action: NotifyDiscordAction, mediaItem: LibraryItem) {
        log.info("Executing NOTIFY_DISCORD action on: {}", mediaItem.libraryPath)
        log.info("Discord message: {}", action.message)
        log.warn("NOTIFY_DISCORD action execution requires Discord webhook integration")
    }

    private fun executeLog(action: LogAction, mediaItem: LibraryItem) {
        val logMessage = "${action.message} - Media: ${mediaItem.libraryPath}"
        when (action.level) {
            LogLevel.DEBUG -> log.debug(logMessage)
            LogLevel.INFO -> log.info(logMessage)
            LogLevel.WARN -> log.warn(logMessage)
            LogLevel.ERROR -> log.error(logMessage)
        }
    }

    private fun executeRemoveFromSonarr(action: RemoveFromSonarrAction, mediaItem: LibraryItem) {
        log.info("Executing REMOVE_FROM_SONARR action on: {}", mediaItem.libraryPath)
        log.warn("REMOVE_FROM_SONARR action execution requires integration with Sonarr service")
    }

    private fun executeRemoveFromRadarr(action: RemoveFromRadarrAction, mediaItem: LibraryItem) {
        log.info("Executing REMOVE_FROM_RADARR action on: {}", mediaItem.libraryPath)
        log.warn("REMOVE_FROM_RADARR action execution requires integration with Radarr service")
    }

    private fun executeAddToExclusion(action: AddToExclusionAction, mediaItem: LibraryItem) {
        log.info("Executing ADD_TO_EXCLUSION action on: {}", mediaItem.libraryPath)
        if (action.reason != null) {
            log.info("Exclusion reason: {}", action.reason)
        }
        log.warn("ADD_TO_EXCLUSION action execution requires integration with *arr services")
    }
}
