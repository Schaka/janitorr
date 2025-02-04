package com.github.schaka.janitorr.jellystat

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.jellystat.requests.ItemRequest
import com.github.schaka.janitorr.jellystat.requests.JellystatPage
import com.github.schaka.janitorr.jellystat.requests.WatchHistoryResponse
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@RegisterReflectionForBinding(classes = [ItemRequest::class, WatchHistoryResponse::class, JellystatPage::class])
@Configuration(proxyBeanMethods = false)
class JellystatConfig(
        val jellystatClient: JellystatClient,
        val mediaServerService: AbstractMediaServerService
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun jellystatService(jellystatProperties: JellystatProperties, applicationProperties: ApplicationProperties): JellystatService {
        return if (jellystatProperties.enabled) JellystatRestService(jellystatClient, jellystatProperties, mediaServerService, applicationProperties) else JellystatNoOpService()
    }

}