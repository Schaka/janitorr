package com.github.schaka.janitorr.mediaserver.library

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter

data class ProviderIds(
        val Tvdb: String? = null,
        val Imdb: String? = null,
        val Tmdb: String? = null,
        @JsonAnySetter
        @get:JsonAnyGetter
        val otherFields: Map<String, Any> = hashMapOf()
)