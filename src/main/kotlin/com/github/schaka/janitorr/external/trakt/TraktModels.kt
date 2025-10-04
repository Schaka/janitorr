package com.github.schaka.janitorr.external.trakt

data class TraktMovieResponse(
    val title: String,
    val year: Int,
    val ids: TraktIds,
    val rating: Double?,
    val votes: Int?
)

data class TraktShowResponse(
    val title: String,
    val year: Int?,
    val ids: TraktIds,
    val rating: Double?,
    val votes: Int?
)

data class TraktIds(
    val trakt: Int,
    val slug: String,
    val imdb: String?,
    val tmdb: Int?,
    val tvdb: Int?
)

data class TraktTrendingItem(
    val watchers: Int,
    val movie: TraktMovieResponse?,
    val show: TraktShowResponse?
)

data class TraktStatsResponse(
    val watchers: Int,
    val plays: Int,
    val collectors: Int,
    val comments: Int,
    val lists: Int,
    val votes: Int
)
