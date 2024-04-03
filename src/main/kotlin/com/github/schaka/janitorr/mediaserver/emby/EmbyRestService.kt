package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient

open class EmbyRestService(

        @Emby embyClient: MediaServerClient,
        @Emby embyUserClient: MediaServerUserClient,
        embyProperties: EmbyProperties,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties

) : AbstractMediaServerRestService("Emby", embyClient, embyUserClient, embyProperties, applicationProperties, fileSystemProperties)