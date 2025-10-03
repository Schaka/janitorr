package com.github.schaka.janitorr.external.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration

@ConfigurationProperties(prefix = "external-apis")
data class ExternalDataProperties(
    val enabled: Boolean = false,
    @NestedConfigurationProperty
    val tmdb: TmdbProperties = TmdbProperties(),
    @NestedConfigurationProperty
    val omdb: OmdbProperties = OmdbProperties(),
    @NestedConfigurationProperty
    val trakt: TraktProperties = TraktProperties(),
    @NestedConfigurationProperty
    val scoring: ScoringWeights = ScoringWeights(),
    val cacheRefreshInterval: Duration = Duration.ofHours(24)
)

data class TmdbProperties(
    val enabled: Boolean = false,
    val apiKey: String = "",
    val baseUrl: String = "https://api.themoviedb.org/3"
)

data class OmdbProperties(
    val enabled: Boolean = false,
    val apiKey: String = "",
    val baseUrl: String = "http://www.omdbapi.com"
)

data class TraktProperties(
    val enabled: Boolean = false,
    val clientId: String = "",
    val clientSecret: String = "",
    val baseUrl: String = "https://api.trakt.tv"
)

data class ScoringWeights(
    val tmdbRatingWeight: Double = 0.25,
    val imdbRatingWeight: Double = 0.25,
    val popularityWeight: Double = 0.20,
    val trendingWeight: Double = 0.15,
    val availabilityWeight: Double = 0.10,
    val collectibilityWeight: Double = 0.05
)
