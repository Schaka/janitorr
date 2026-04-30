package com.github.schaka.janitorr.mediaserver.library.items

import com.github.schaka.janitorr.mediaserver.library.ProviderIds

data class MediaFolderItem(
        val CollectionType: String?,
        val Container: String?,
        val DateCreated: String,
        val DateLastMediaAdded: String?,
        val EpisodeCount: Int?,
        val ExtraType: String?,
        val Id: String,
        val IsFolder: Boolean,
        val IsoType: String?,
        val LocationType: String?,
        val MediaSources: List<Any>?,
        val MediaStreams: List<Any>?,
        val MediaType: String?,
        val Name: String,
        val OriginalTitle: String?,
        val ParentId: String,
        val Path: String?,
        val ProductionYear: Int?,
        val ProviderIds: ProviderIds,
        val SeasonId: String?,
        val SeasonName: String?,
        val SeriesId: String?,
        val SeriesName: String?,
        val SortName: String,
        val SourceType: String?,
        val Status: String?,
        val Type: String,
        val VideoType: String?
)