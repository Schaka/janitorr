package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

/**
 * Component that manages the run-once behavior of Janitorr.
 * 
 * IMPORTANT: This component is excluded from the "leyden" profile (@Profile("!leyden")).
 * The "leyden" profile is only for build-time AOT cache generation and should never be active at runtime.
 */
@Profile("!leyden")
@Component
class RunOnce(
    val applicationProperties: ApplicationProperties,
    var hasMediaCleanupRun: Boolean = false,
    var hasTagBasedCleanupRun: Boolean = false,
    var hasWeeklyEpisodeCleanupRun: Boolean = false
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Scheduled(fixedDelay = 1000)
    fun run() {
        if (applicationProperties.runOnce && hasMediaCleanupRun && hasTagBasedCleanupRun && hasWeeklyEpisodeCleanupRun) {
            log.info("Run once enabled, all cleanups run. Terminating process.")
            exitProcess(0)
        }
    }

}