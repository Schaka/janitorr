package com.github.schaka.janitorr.mediaserver.library

data class LibraryContent(
        val Id: String,
        val IsFolder: Boolean,
        val IsMovie: Boolean,
        val IsSeries: Boolean,
        val Name: String,
        val Type: String,
        var ProviderIds: ProviderIds? = null,
        val SeasonId: String? = null,
        val SeasonName: String? = null,
        val IndexNumber: Int = 0,
        val SeriesId: String? = null,
        val SeriesName: String? = null,
)