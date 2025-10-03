package com.github.schaka.janitorr.external.common

import com.github.schaka.janitorr.servarr.LibraryItem
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * No-op implementation when external APIs are disabled.
 */
@Service
@ConditionalOnProperty(prefix = "external-apis", name = ["enabled"], havingValue = "false", matchIfMissing = true)
class ExternalDataNoOpService : ExternalDataServiceInterface {

    override fun enrichMediaData(item: LibraryItem): MediaIntelligence {
        return MediaIntelligence.empty()
    }

    override fun shouldPreserveMedia(intelligence: MediaIntelligence): Boolean {
        return false
    }
}

/**
 * Interface for external data services to ensure consistent API.
 */
interface ExternalDataServiceInterface {
    fun enrichMediaData(item: LibraryItem): MediaIntelligence
    fun shouldPreserveMedia(intelligence: MediaIntelligence): Boolean
}
