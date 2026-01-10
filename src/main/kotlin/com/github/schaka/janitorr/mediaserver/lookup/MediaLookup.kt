package com.github.schaka.janitorr.mediaserver.lookup

data class MediaLookup(
    val id: Int, // the ID (i.e. ID in Sonarr or Radarr)
    val season: SeasonInfo = SeasonInfo()
) {
    constructor(id: Int, seasonNumber: Int?) : this(
        id,
        seasonNumber?.let { SeasonInfo(it) } ?: SeasonInfo()
    )
}
