package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class RunOnce(
    val applicationProperties: ApplicationProperties,
    var hasMediaCleanupRun: Boolean,
    var hasTagBasedCleanupRun: Boolean,
    var hasWeeklyEpisodeCleanupRun: Boolean
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