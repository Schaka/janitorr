package com.github.schaka.janitorr.mediaserver.emby.library

import com.github.schaka.janitorr.mediaserver.library.TypeOption

data class LibraryOptions(
        val PathInfos: List<PathInfo>,
        val ContentType: String,
        val TypeOptions: List<TypeOption>? = null,
)