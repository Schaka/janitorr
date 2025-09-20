package com.github.schaka.janitorr.stats.config

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.stats.StatsNoOpService
import com.github.schaka.janitorr.stats.StatsService
import com.github.schaka.janitorr.stats.jellystat.JellystatClient
import com.github.schaka.janitorr.stats.jellystat.JellystatProperties
import com.github.schaka.janitorr.stats.jellystat.JellystatRestService
import com.github.schaka.janitorr.stats.streamystats.StreamystatsClient
import com.github.schaka.janitorr.stats.streamystats.StreamystatsProperties
import com.github.schaka.janitorr.stats.streamystats.StreamystatsRestService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class StatsConfig(
    val streamystatsClient: StreamystatsClient,
    val jellystatClient: JellystatClient,
    val mediaServerService: AbstractMediaServerService
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Bean
    fun statsService(
        jellystatProperties: JellystatProperties,
        streamystatsProperties: StreamystatsProperties,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties,
    ): StatsService {

        if (!jellystatProperties.enabled && !streamystatsProperties.enabled) {
            return StatsNoOpService()
        }

        if (jellystatProperties.enabled && streamystatsProperties.enabled) {
            throw IllegalStateException("Both Jellystat and Streamystats CANNOT be enabled!")
        }

        if (jellystatProperties.enabled) {
            return JellystatRestService(jellystatClient, jellystatProperties, mediaServerService, applicationProperties)
        }

        return StreamystatsRestService(streamystatsClient, streamystatsProperties, mediaServerService, applicationProperties)
    }

}