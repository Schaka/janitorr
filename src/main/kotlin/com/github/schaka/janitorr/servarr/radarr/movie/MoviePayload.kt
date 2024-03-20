package com.github.schaka.janitorr.servarr.radarr.movie

data class MoviePayload(
        val added: String,
        val cleanTitle: String?,
        val folder: String?,
        val folderName: String?,
        val hasFile: Boolean,
        val id: Int,
        val imdbId: String?,
        val inCinemas: String?,
        var monitored: Boolean,
        val movieFile: MovieFile?,
        val originalTitle: String?,
        val path: String,
        val qualityProfileId: Int,
        val rootFolderPath: String?,
        val sortTitle: String?,
        val status: String?,
        val tags: List<Int>,
        val title: String,
        val titleSlug: String,
        val tmdbId: Int,
        val year: Int
)