package com.github.schaka.janitorr.stats.streamystats

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.stats.jellystat.requests.ItemRequest
import com.github.schaka.janitorr.stats.jellystat.requests.JellystatPage
import com.github.schaka.janitorr.stats.jellystat.requests.WatchHistoryResponse
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.stats.StatsNoOpService
import com.github.schaka.janitorr.stats.StatsService
import com.github.schaka.janitorr.stats.jellystat.JellystatClient
import com.github.schaka.janitorr.stats.jellystat.JellystatProperties
import com.github.schaka.janitorr.stats.jellystat.JellystatRestService
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@RegisterReflectionForBinding(classes = [ItemRequest::class, WatchHistoryResponse::class, JellystatPage::class])
@Configuration(proxyBeanMethods = false)
class StreamystatsConfig(
    val streamystatsClient: StreamystatsClient,
    val mediaServerService: AbstractMediaServerService
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun streamystatsService(streamystatsProperties: StreamystatsProperties, applicationProperties: ApplicationProperties): StatsService {
        return if (streamystatsProperties.enabled) StreamystatsRestService(
            streamystatsClient,
            streamystatsProperties,
            mediaServerService,
            applicationProperties
        ) else StatsNoOpService()
    }

}