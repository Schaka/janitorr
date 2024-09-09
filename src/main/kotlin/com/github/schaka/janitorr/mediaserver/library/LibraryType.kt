package com.github.schaka.janitorr.mediaserver.library

import com.github.schaka.janitorr.mediaserver.MediaServerProperties
import kotlin.reflect.KProperty1

enum class LibraryType(
    val collectionType: String,
    val collectionName: KProperty1<MediaServerProperties, String>,
    val folderName: String) {

    MOVIES("Movies", MediaServerProperties::leavingSoonMovies, "movies"),
    TV_SHOWS("TvShows", MediaServerProperties::leavingSoonTv, "tv")

}