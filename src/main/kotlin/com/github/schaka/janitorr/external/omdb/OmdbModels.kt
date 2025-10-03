package com.github.schaka.janitorr.external.omdb

data class OmdbResponse(
    val Title: String,
    val Year: String,
    val imdbRating: String?,
    val imdbVotes: String?,
    val Metascore: String?,
    val imdbID: String,
    val Type: String,
    val BoxOffice: String?,
    val Awards: String?,
    val Response: String,
    val Error: String?
)
