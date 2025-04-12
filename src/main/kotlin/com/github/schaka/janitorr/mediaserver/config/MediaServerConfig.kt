package com.github.schaka.janitorr.mediaserver.config

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerService
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.emby.Emby
import com.github.schaka.janitorr.mediaserver.emby.EmbyMediaServerClient
import com.github.schaka.janitorr.mediaserver.emby.EmbyProperties
import com.github.schaka.janitorr.mediaserver.emby.EmbyRestService
import com.github.schaka.janitorr.mediaserver.emby.library.AddMediaPathRequest
import com.github.schaka.janitorr.mediaserver.emby.library.AddVirtualFolder
import com.github.schaka.janitorr.mediaserver.jellyfin.Jellyfin
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinProperties
import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.mediaserver.library.*
import com.github.schaka.janitorr.mediaserver.library.items.ItemPage
import com.github.schaka.janitorr.mediaserver.library.items.MediaFolderItem
import com.github.schaka.janitorr.servarr.bazarr.BazarrService
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Only required for native image
 */
@RegisterReflectionForBinding(classes = [ItemPage::class, MediaFolderItem::class, LibraryContent::class,
    VirtualFolderResponse::class, ProviderIds::class, UserData::class, CollectionResponse::class, AddLibraryRequest::class,
    AddVirtualFolder::class, AddPathRequest::class, AddMediaPathRequest::class,
])
@Configuration(proxyBeanMethods = false)
class MediaServerConfig(
    @Emby val embyClient: EmbyMediaServerClient,
    @Jellyfin val jellyfinClient: MediaServerClient,
    @Emby val embyUserClient: MediaServerUserClient,
    @Jellyfin val jellyfinUserClient: MediaServerUserClient,
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

        if (!jellyfinProperties.enabled && !embyProperties.enabled) {
            throw IllegalStateException("Both Emby and Jellyfin CANNOT be enabled!")
        }

        if (embyProperties.enabled) {
            return EmbyRestService(embyClient, embyUserClient, bazarrService, embyProperties, applicationProperties, fileSystemProperties)
        }

        return JellyfinRestService(jellyfinClient, jellyfinUserClient, bazarrService, jellyfinProperties, applicationProperties, fileSystemProperties)
    }
}