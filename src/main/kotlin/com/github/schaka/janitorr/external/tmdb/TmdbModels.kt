package com.github.schaka.janitorr.external.tmdb

data class TmdbMovieResponse(
    val id: Int,
    val title: String,
    val vote_average: Double,
    val vote_count: Int,
    val popularity: Double,
    val release_date: String?,
    val genres: List<TmdbGenre>?,
    val belongs_to_collection: TmdbCollection?
)

data class TmdbTvResponse(
    val id: Int,
    val name: String,
    val vote_average: Double,
    val vote_count: Int,
    val popularity: Double,
    val first_air_date: String?,
    val genres: List<TmdbGenre>?
)

data class TmdbGenre(
    val id: Int,
    val name: String
)

data class TmdbCollection(
    val id: Int,
    val name: String
)

data class TmdbTrendingResponse(
    val page: Int,
    val results: List<TmdbTrendingItem>
)

data class TmdbTrendingItem(
    val id: Int,
    val media_type: String,
    val title: String?,
    val name: String?,
    val vote_average: Double,
    val popularity: Double
)

data class TmdbExternalIds(
    val imdb_id: String?,
    val tvdb_id: Int?
)
