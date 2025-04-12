package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.mediaserver.library.LibraryType

enum class LeavingSoonType {
    MOVIES,
    TV,
    MOVIES_AND_TV,
    NONE;

    fun isAllowedForLibraryType(libraryType: LibraryType): Boolean {
        return when (libraryType) {
            LibraryType.MOVIES -> this == MOVIES_AND_TV || this == MOVIES
            LibraryType.TV_SHOWS -> this == MOVIES_AND_TV || this == TV
        }
    }
}