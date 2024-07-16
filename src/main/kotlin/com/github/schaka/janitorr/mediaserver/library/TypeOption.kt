package com.github.schaka.janitorr.mediaserver.library

data class TypeOption(
        val ImageFetcherOrder: List<String>,
        val ImageFetchers: List<String>,
        val ImageOptions: List<Any>,
        val MetadataFetcherOrder: List<String>,
        val MetadataFetchers: List<String>,
        val Type: String
)