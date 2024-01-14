package com.github.schaka.janitorr.servarr.radarr.movie

data class SelectOption(
    val dividerAfter: Boolean,
    val hint: String,
    val name: String,
    val order: Int,
    val value: Int
)