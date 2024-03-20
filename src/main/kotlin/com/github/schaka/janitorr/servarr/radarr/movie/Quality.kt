package com.github.schaka.janitorr.servarr.radarr.movie

data class Quality(
        val id: Int,
        val modifier: String,
        val name: String,
        val resolution: Int,
        val source: String
)