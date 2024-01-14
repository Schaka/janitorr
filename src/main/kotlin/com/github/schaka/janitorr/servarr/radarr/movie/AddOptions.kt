package com.github.schaka.janitorr.servarr.radarr.movie

data class AddOptions(
    val addMethod: String,
    val ignoreEpisodesWithFiles: Boolean,
    val ignoreEpisodesWithoutFiles: Boolean,
    val monitor: String,
    val searchForMovie: Boolean
)