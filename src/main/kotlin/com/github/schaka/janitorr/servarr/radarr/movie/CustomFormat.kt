package com.github.schaka.janitorr.servarr.radarr.movie

data class CustomFormat(
    val id: Int,
    val includeCustomFormatWhenRenaming: Boolean,
    val name: String,
    val specifications: List<Specification>
)