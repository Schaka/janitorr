package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("clients.emby.enabled", havingValue = "true", matchIfMissing = false)
class EmbyRestService(

        @Emby embyClient: MediaServerClient,
        @Emby embyUserClient: MediaServerUserClient,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties

) : AbstractMediaServerRestService("Emby", embyClient, embyUserClient, applicationProperties, fileSystemProperties) {

}