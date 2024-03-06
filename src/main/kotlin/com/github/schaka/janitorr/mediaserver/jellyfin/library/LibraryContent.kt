package com.github.schaka.janitorr.mediaserver.jellyfin.library

data class LibraryContent(
        val Audio: String?,
        val BackdropImageTags: List<String>?,
        val ChildCount: Int,
        val CollectionType: String?,
        val Container: String?,
        val DateCreated: String?,
        val DateLastMediaAdded: String?,
        val EndDate: String?,
        val EpisodeCount: Int?,
        val EpisodeTitle: String?,
        val Etag: String?,
        val ExternalUrls: List<ExternalUrl>?,
        val ExtraType: String?,
        val Genres: List<String>?,
        val Id: String,
        val ImageBlurHashes: ImageBlurHashes,
        val IndexNumber: Int,
        val IndexNumberEnd: Int,
        val IsFolder: Boolean,
        val IsHD: Boolean,
        val IsLive: Boolean,
        val IsMovie: Boolean,
        val IsPlaceHolder: Boolean,
        val IsRepeat: Boolean,
        val IsSeries: Boolean,
        val LocationType: String,
        val LockData: Boolean,
        val MediaType: String?,
        val MovieCount: Int?,
        val Name: String,
        val Number: String?,
        val OriginalTitle: String?,
        val Path: String?,
        var ProviderIds: ProviderIds?,
        val RecursiveItemCount: Int,
        val SeasonId: String?,
        val SeasonName: String?,
        val SeriesCount: Int?,
        val SeriesId: String?,
        val SeriesName: String?,
        val SortName: String?,
        val StartDate: String?,
        val Status: String?,
        val Type: String,
        val UserData: UserData?,
        val VideoType: String?,
)