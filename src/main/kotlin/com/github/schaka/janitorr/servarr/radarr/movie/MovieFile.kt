package com.github.schaka.janitorr.servarr.radarr.movie

data class MovieFile(
        val customFormatScore: Int?,
        val customFormats: List<CustomFormat>?,
        val dateAdded: String,
        val edition: String?,
        val id: Int,
        val indexerFlags: Int,
        val languages: List<Language>,
        val movieId: Int,
        val originalFilePath: String?,
        val path: String,
        val quality: Quality,
        val qualityCutoffNotMet: Boolean,
        val relativePath: String,
        val releaseGroup: String?,
        val sceneName: String?,
        val size: Long
)