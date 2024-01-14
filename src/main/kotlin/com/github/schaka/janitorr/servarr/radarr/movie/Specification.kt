package com.github.schaka.janitorr.servarr.radarr.movie

data class Specification(
    val fields: List<Field>,
    val id: Int,
    val implementation: String,
    val implementationName: String,
    val infoLink: String,
    val name: String,
    val negate: Boolean,
    val presets: List<String>,
    val required: Boolean
)