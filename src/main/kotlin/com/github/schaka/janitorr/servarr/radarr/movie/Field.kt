package com.github.schaka.janitorr.servarr.radarr.movie

data class Field(
        val advanced: Boolean,
        val helpLink: String,
        val helpText: String,
        val helpTextWarning: String,
        val hidden: String,
        val isFloat: Boolean,
        val label: String,
        val name: String,
        val order: Int,
        val placeholder: String,
        val privacy: String,
        val section: String,
        val selectOptions: List<SelectOption>,
        val selectOptionsProviderAction: String,
        val type: String,
        val unit: String,
        val value: String
)