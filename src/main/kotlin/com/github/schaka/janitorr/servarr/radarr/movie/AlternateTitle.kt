package com.github.schaka.janitorr.servarr.radarr.movie

data class AlternateTitle(
    val cleanTitle: String?,
    val id: Int,
    val movieMetadataId: Int,
    val sourceType: String,
    val title: String
)