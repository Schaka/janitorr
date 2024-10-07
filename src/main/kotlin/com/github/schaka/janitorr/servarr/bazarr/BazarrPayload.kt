package com.github.schaka.janitorr.servarr.bazarr

data class BazarrPayload (
    val episode: Int?,
    val season: Int?,
    val subtitles: List<Subtitles>
)