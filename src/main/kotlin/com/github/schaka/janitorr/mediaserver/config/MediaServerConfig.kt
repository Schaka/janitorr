package com.github.schaka.janitorr.mediaserver.config

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.emby.Emby
import com.github.schaka.janitorr.mediaserver.emby.EmbyProperties
import com.github.schaka.janitorr.mediaserver.emby.EmbyRestService
import com.github.schaka.janitorr.mediaserver.jellyfin.Jellyfin
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Only required for native image
 */
@Configuration(proxyBeanMethods = false)
class MediaServerConfig(
    @Emby val embyClient: MediaServerClient,
    @Jellyfin val jellyfinClient: MediaServerClient,
    @Emby val embyUserClient: MediaServerUserClient,
    @Jellyfin val jellyfinUserClient: MediaServerUserClient,
) {

    @Bean
    fun mediaServer(
        jellyfinProperties: JellyfinProperties,
        embyProperties: EmbyProperties,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties
    ): MediaServerService {

        if (!jellyfinProperties.enabled && !embyProperties.enabled) {
            return MediaServerNoOpService()
        }

        if (!jellyfinProperties.enabled && !embyProperties.enabled) {
            throw IllegalStateException("Both Emby and Jellyfin CANNOT be enabled!")
        }

        if (embyProperties.enabled) {
            return EmbyRestService(embyClient, embyUserClient, applicationProperties, fileSystemProperties)
        }

        return JellyfinRestService(jellyfinClient, jellyfinUserClient, applicationProperties, fileSystemProperties)
    }
}