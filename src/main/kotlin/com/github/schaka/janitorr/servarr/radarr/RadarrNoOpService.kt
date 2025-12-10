package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Radarr
@ConditionalOnProperty(prefix = "clients.radarr.enabled", havingValue = "false", matchIfMissing = true)
@Service
class RadarrNoOpService : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun getEntries(): List<LibraryItem> {
        log.info("Radarr is disabled, not getting any movies")
        return listOf()
    }

    override fun removeEntries(items: List<LibraryItem>) {
        log.info("Radarr is disabled, not deleting any movies")
    }

}