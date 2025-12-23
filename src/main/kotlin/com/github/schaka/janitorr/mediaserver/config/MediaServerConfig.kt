package com.github.schaka.janitorr.mediaserver.config

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerLibraryQueryService
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.emby.Emby
import com.github.schaka.janitorr.mediaserver.emby.EmbyMediaServerClient
import com.github.schaka.janitorr.mediaserver.emby.EmbyProperties
import com.github.schaka.janitorr.mediaserver.emby.EmbyRestService
import com.github.schaka.janitorr.mediaserver.jellyfin.Jellyfin
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.servarr.bazarr.BazarrService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class MediaServerConfig(
    @Emby val embyClient: EmbyMediaServerClient,
    @Jellyfin val jellyfinClient: MediaServerClient,
    @Emby val embyUserClient: MediaServerUserClient,
    @Jellyfin val jellyfinUserClient: MediaServerUserClient,
    val mediaServerLibraryQueryService: MediaServerLibraryQueryService,
) {

    @Bean
    fun mediaServer(
        jellyfinProperties: JellyfinProperties,
        embyProperties: EmbyProperties,
        bazarrService: BazarrService,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties,
    ): AbstractMediaServerService {

        if (!jellyfinProperties.enabled && !embyProperties.enabled) {
            return MediaServerNoOpService()
        }

        if (jellyfinProperties.enabled && embyProperties.enabled) {
            throw IllegalStateException("Both Emby and Jellyfin CANNOT be enabled!")
        }

        if (embyProperties.enabled) {
            return EmbyRestService(embyClient, embyUserClient, bazarrService, mediaServerLibraryQueryService,embyProperties, applicationProperties, fileSystemProperties)
        }

        return JellyfinRestService(jellyfinClient, jellyfinUserClient, bazarrService, mediaServerLibraryQueryService, jellyfinProperties, applicationProperties, fileSystemProperties)
    }
}