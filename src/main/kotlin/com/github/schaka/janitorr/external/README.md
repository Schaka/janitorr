# External API Integration

This package provides integration with external APIs (TMDB, OMDb, Trakt) to enable intelligent cleanup decisions based on ratings, popularity, and trending data.

## Package Structure

```
external/
├── common/                      # Shared services and configuration
│   ├── ExternalApiConfig.kt     # Feign client configuration
│   ├── ExternalDataCacheConfig.kt # Caffeine cache configuration
│   ├── ExternalDataProperties.kt  # Configuration properties
│   ├── ExternalDataService.kt   # Main orchestration service
│   ├── ExternalDataNoOpService.kt # No-op implementation when disabled
│   ├── MediaIntelligence.kt     # Intelligence score data class
│   ├── TmdbService.kt          # TMDB-specific logic
│   ├── OmdbService.kt          # OMDb-specific logic
│   └── TraktService.kt         # Trakt-specific logic
├── tmdb/                       # TMDB client and models
│   ├── TmdbClient.kt           # Feign interface for TMDB API
│   └── TmdbModels.kt           # Data classes for TMDB responses
├── omdb/                       # OMDb client and models
│   ├── OmdbClient.kt           # Feign interface for OMDb API
│   └── OmdbModels.kt           # Data classes for OMDb responses
└── trakt/                      # Trakt client and models
    ├── TraktClient.kt          # Feign interface for Trakt API
    └── TraktModels.kt          # Data classes for Trakt responses
```

## Architecture

### Service Layer

1. **ExternalDataService**: Main orchestration service that:
   - Coordinates calls to TMDB, OMDb, and Trakt services
   - Calculates weighted intelligence scores
   - Determines preservation decisions
   - Handles errors gracefully

2. **Individual Services** (TmdbService, OmdbService, TraktService):
   - Encapsulate API-specific logic
   - Handle response parsing
   - Normalize data to common scales
   - Provide defensive error handling

3. **ExternalDataNoOpService**: No-operation implementation:
   - Used when external APIs are disabled
   - Returns empty intelligence data
   - Ensures graceful degradation

### Client Layer

Each API has a Feign client interface:
- **TmdbClient**: TMDB API v3 endpoints
- **OmdbClient**: OMDb API endpoints
- **TraktClient**: Trakt API v2 endpoints

Clients are configured via `ExternalApiConfig` with:
- Base URLs
- Request interceptors (auth headers)
- Jackson JSON encoding/decoding

### Data Models

#### MediaIntelligence
Core data class containing all intelligence metrics:
```kotlin
data class MediaIntelligence(
    val tmdbRating: Double?,        // 0-10
    val imdbRating: Double?,        // 0-10
    val popularityScore: Double?,   // 0-100
    val trendingScore: Double?,     // 0-100
    val availabilityScore: Double?, // 0-100
    val collectibilityScore: Double?, // 0-100
    val overallScore: Double        // Weighted composite 0-100
)
```

#### API-specific Models
Each API has its own response models matching the API structure:
- TMDB: Movie, TV, Trending, ExternalIds responses
- OMDb: Single response with rating, awards, etc.
- Trakt: Stats, Trending, Search responses

## Configuration

### Properties
```kotlin
@ConfigurationProperties(prefix = "external-apis")
data class ExternalDataProperties(
    val enabled: Boolean = false,
    val tmdb: TmdbProperties,
    val omdb: OmdbProperties,
    val trakt: TraktProperties,
    val scoring: ScoringWeights,
    val cacheRefreshInterval: Duration
)
```

### Conditional Loading
Services are only loaded when:
- `external-apis.enabled=true` (main switch)
- Individual API enabled (e.g., `external-apis.tmdb.enabled=true`)

Uses Spring's `@ConditionalOnProperty` for conditional bean creation.

### Caching
- Cache name: `external-data-cache`
- TTL: Configurable via `cache-refresh-interval` (default 24h)
- Max size: 1000 entries
- Cache key: `tmdbId` > `imdbId` > `tvdbId` (first available)

## Scoring Algorithm

### Weighted Composite Score
```kotlin
overallScore = (
    (tmdbRating * 10) * tmdbWeight +
    (imdbRating * 10) * imdbWeight +
    popularityScore * popularityWeight +
    trendingScore * trendingWeight +
    collectibilityScore * collectibilityWeight
) / totalWeight
```

### Preservation Rules
Content is automatically preserved if:
1. IMDb rating ≥ 8.0
2. TMDB rating ≥ 8.0
3. Trending score ≥ 75
4. Collectibility score ≥ 80
5. Overall score ≥ 70

## Usage

### Basic Usage
```kotlin
@Service
class CleanupService(
    private val externalDataService: ExternalDataServiceInterface
) {
    fun shouldDelete(item: LibraryItem): Boolean {
        val intelligence = externalDataService.enrichMediaData(item)
        return !externalDataService.shouldPreserveMedia(intelligence)
    }
}
```

### Custom Logic
```kotlin
fun analyzeItem(item: LibraryItem) {
    val intelligence = externalDataService.enrichMediaData(item)
    
    when {
        intelligence.imdbRating?.let { it >= 8.0 } == true ->
            log.info("High rated content: ${item.filePath}")
        
        intelligence.trendingScore?.let { it >= 75 } == true ->
            log.info("Trending content: ${item.filePath}")
        
        intelligence.overallScore >= 70 ->
            log.info("High intelligence score: ${item.filePath}")
    }
}
```

## Error Handling

### Graceful Degradation
- Individual API failures don't stop processing
- Missing data treated as null
- Empty intelligence returned on total failure
- Logs warnings at DEBUG level

### Null Safety
All API response fields are nullable:
```kotlin
val rating = intelligence.tmdbRating ?: 0.0  // Safe handling
```

## Testing

### Unit Tests
- `ExternalDataServiceTest`: Service orchestration tests
- `MediaIntelligenceTest`: Data class tests
- Mock all external clients with MockK

### Integration Tests
Future enhancement: Test with actual API calls using test keys.

## Performance

### Optimization Strategies
1. **Caching**: 24-hour cache reduces API calls
2. **Lazy Loading**: Only call APIs when data needed
3. **Parallel Processing**: Services can be called concurrently
4. **Null Checks**: Skip API calls if IDs missing

### Resource Usage
- Memory: ~1000 cached entries × ~500 bytes = ~500KB
- Network: Minimal due to caching
- CPU: Negligible scoring overhead

## Future Enhancements

### Planned Features
1. **JustWatch Integration**: Streaming availability data
2. **Availability Scoring**: Use streaming data in intelligence
3. **Personal Stats**: User watchlist/collection from Trakt
4. **Batch Enrichment**: Bulk API calls for efficiency
5. **Metrics**: Track API usage and decisions

### API Expansion
Additional APIs to consider:
- Letterboxd for film ratings
- TVDb for TV show metadata
- AniDB for anime metadata

## Troubleshooting

### Common Issues

**APIs not being called:**
- Check `external-apis.enabled=true`
- Verify individual API `enabled=true`
- Confirm API keys are valid

**Empty intelligence scores:**
- Check item has tmdbId, imdbId, or tvdbId
- Verify API responses in DEBUG logs
- Check cache TTL hasn't expired

**High API usage:**
- Increase `cache-refresh-interval`
- Reduce cleanup frequency
- Disable less critical APIs

## Contributing

When adding new APIs:
1. Create client interface in new package
2. Add data models for responses
3. Create service class in common/
4. Add properties to ExternalDataProperties
5. Update ExternalApiConfig bean creation
6. Add to ExternalDataService orchestration
7. Update documentation
8. Add unit tests

## References

- [TMDB API Documentation](https://developers.themoviedb.org/3)
- [OMDb API Documentation](http://www.omdbapi.com/)
- [Trakt API Documentation](https://trakt.docs.apiary.io/)
