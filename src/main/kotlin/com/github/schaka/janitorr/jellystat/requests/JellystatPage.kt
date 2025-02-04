package com.github.schaka.janitorr.jellystat.requests

import com.fasterxml.jackson.annotation.JsonProperty

data class JellystatPage<T>(
    @JsonProperty("current_page")
    val currentPage: Long,
    val pages: Long,
    val sort: String,
    val desc: Boolean,
    val results: List<T>
)
