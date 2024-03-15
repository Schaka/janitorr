package com.github.schaka.janitorr.mediaserver.jellyfin

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.AbstractMediaServerRestService
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.library.AddLibraryRequest
import com.github.schaka.janitorr.mediaserver.library.LibraryContent
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.mediaserver.library.LibraryType.MOVIES
import com.github.schaka.janitorr.mediaserver.library.LibraryType.TV_SHOWS
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path

@Service
@ConditionalOnProperty("clients.jellyfin.enabled", havingValue = "true", matchIfMissing = false)
class JellyfinRestService(

        @Jellyfin jellyfinClient: MediaServerClient,
        @Jellyfin jellyfinUserClient: MediaServerUserClient,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties

) : AbstractMediaServerRestService("Jellyfin", jellyfinClient, jellyfinUserClient, applicationProperties, fileSystemProperties) {

}