package com.github.schaka.janitorr.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ManagementUiLogger(
    private val environment: Environment,
    private val managementUiProperties: ManagementUiProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(ManagementUiLogger::class.java)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun logManagementUiStatus() {
        val uiEnabled = managementUiProperties.enabled
        val serverPort = environment.getProperty("server.port", "8080")
        val profilesActive = environment.getProperty("spring.profiles.active", "default")
        val isLeydenProfile = profilesActive.contains("leyden")

        when {
            isLeydenProfile -> {
                log.info("Management UI is DISABLED (leyden profile active - native image compilation)")
            }
            !uiEnabled -> {
                log.info("Management UI is DISABLED by configuration (management.ui.enabled=false)")
            }
            else -> {
                log.info("Management UI is ENABLED and available at http://localhost:$serverPort/")
                log.info("Management API endpoints available at http://localhost:$serverPort/api/management/")
            }
        }
    }
}
