package com.github.schaka.janitorr.mediaserver.jellyfin

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient

open class JellyfinRestService(

        @Jellyfin jellyfinClient: MediaServerClient,
        @Jellyfin jellyfinUserClient: MediaServerUserClient,
        jellyfinProperties: JellyfinProperties,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties

) : AbstractMediaServerRestService("Jellyfin", jellyfinClient, jellyfinUserClient, jellyfinProperties, applicationProperties, fileSystemProperties)