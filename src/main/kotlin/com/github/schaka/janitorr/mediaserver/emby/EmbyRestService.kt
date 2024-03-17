package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("clients.emby.enabled", havingValue = "true", matchIfMissing = false)
class EmbyRestService(

        @Emby embyClient: MediaServerClient,
        @Emby embyUserClient: MediaServerUserClient,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties

) : AbstractMediaServerRestService("Emby", embyClient, embyUserClient, applicationProperties, fileSystemProperties)