package com.github.schaka.janitorr.servarr.radarr.movie

data class Ratings(
    val imdb: Imdb?,
    val metacritic: Metacritic?,
    val rottenTomatoes: RottenTomatoes?,
    val tmdb: Tmdb
)