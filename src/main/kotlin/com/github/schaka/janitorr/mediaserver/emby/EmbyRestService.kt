package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.mediaserver.MediaServerService
import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("clients.emby.enabled", havingValue = "true")
class EmbyRestService(

        val jellyfinProperties: EmbyProperties,
        val applicationProperties: ApplicationProperties,
        val fileSystemProperties: FileSystemProperties

) : MediaServerService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        private val seasonPattern = Regex("Season (?<season>\\d+)")
        private val filePattern = Regex("^.*\\.(mkv|mp4|avi|webm|mts|m2ts|ts|wmv|mpg|mpeg|mp2|m2v|m4v)\$")
        private val numberPattern = Regex("[0-9]+")
    }

    override fun cleanupTvShows(items: List<LibraryItem>) {
    }

    override fun cleanupMovies(items: List<LibraryItem>) {
    }

    override fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean) {
    }

}