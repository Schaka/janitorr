package com.github.schaka.janitorr.jellyfin.library

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter

data class ProviderIds(
    val Tvdb: String?,
    val Imdb: String?,
    val Tmdb: String?,
    @JsonAnySetter
    @get:JsonAnyGetter
    val otherFields: Map<String, Any> = hashMapOf()
)