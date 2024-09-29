package com.github.schaka.janitorr.servarr

import java.time.LocalDateTime

data class LibraryItem(
        val id: Int,
        val importedDate: LocalDateTime,

        // History only: these 2 names are only accurate for the time of import and don't get updated when filenames change
        val originalPath: String,
        val libraryPath: String,

        val parentPath: String, // points to tv show or a movie, rather than a single episode, trailer, etc: e.g. /data/media/tv/Seinfeld
        val rootFolderPath: String, // points to root folder: e.g. /data/media/tv
        val filePath: String, // points to the ACTUAL folder, e.g. a season, movie subfolder if necessary, etc

        val imdbId: String? = null,
        val tvdbId: Int? = null,
        val tmdbId: Int? = null,
        val season: Int? = null,

        // references the id (usually some uuid) inside Jellyfin/Emby - can refer to a movie, tv show, season or episode
        var mediaServerIds: MutableList<String> = mutableListOf(),
        var seeding: Boolean = false,
        var lastSeen: LocalDateTime? = null,

        val tags: List<String> = listOf()
)
{
    val historyAge: LocalDateTime
        get() = lastSeen ?: importedDate
}