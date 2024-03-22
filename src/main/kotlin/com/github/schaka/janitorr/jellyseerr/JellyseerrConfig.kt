package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.jellyseerr.paging.JellyseerrPage
import com.github.schaka.janitorr.jellyseerr.requests.ModifiedBy
import com.github.schaka.janitorr.jellyseerr.requests.RequestResponse
import com.github.schaka.janitorr.jellyseerr.requests.RequestSeason
import com.github.schaka.janitorr.mediaserver.library.ExternalUrl
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@RegisterReflectionForBinding(classes = [JellyseerrPage::class, RequestResponse::class, RequestSeason::class, ModifiedBy::class, ExternalUrl::class])
@Configuration(proxyBeanMethods = false)
class JellyseerrConfig(
    val jellyseerrClient: JellyseerrClient
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun jellyseerrService(jellyseerrProperties: JellyseerrProperties, applicationProperties: ApplicationProperties): JellyseerrService {
        return if (jellyseerrProperties.enabled) JellyseerrRestService(jellyseerrClient, applicationProperties) else JellyseerrNoOpService()
    }

}