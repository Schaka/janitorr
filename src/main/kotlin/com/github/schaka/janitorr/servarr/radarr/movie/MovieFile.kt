package com.github.schaka.janitorr.servarr.radarr.movie

data class MovieFile(
        val customFormatScore: Int?,
        val customFormats: List<CustomFormat>?,
        val dateAdded: String?,
        val id: Int,
        val movieId: Int,
        val originalFilePath: String?,
        val path: String,
        val quality: QualityWrapper?,
        val relativePath: String,
        val releaseGroup: String?,
        val size: Long?
)