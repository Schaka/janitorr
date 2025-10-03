# External API Integration - Usage Examples

This document provides examples of how to integrate external API intelligence into your cleanup workflows.

## Overview

The external API integration provides intelligent scoring for media items based on:
- TMDB ratings and popularity
- IMDb ratings and awards
- Trakt watch statistics and trending data

## Basic Usage

### 1. Enable External APIs

First, configure your `application.yml`:

```yaml
external-apis:
  enabled: true
  cache-refresh-interval: 24h
  
  tmdb:
    enabled: true
    api-key: "your-tmdb-api-key"
  
  omdb:
    enabled: true
    api-key: "your-omdb-api-key"
  
  trakt:
    enabled: true
    client-id: "your-client-id"
    client-secret: "your-client-secret"
```

### 2. Enriching Media Data

The `ExternalDataService` enriches library items with intelligence scores:

```kotlin
import com.github.schaka.janitorr.external.common.ExternalDataService
import com.github.schaka.janitorr.servarr.LibraryItem

// Inject the service
class CleanupService(
    private val externalDataService: ExternalDataService
) {
    
    fun processLibraryItem(item: LibraryItem) {
        // Get intelligence data for the item
        val intelligence = externalDataService.enrichMediaData(item)
        
        // Check if item should be preserved
        if (externalDataService.shouldPreserveMedia(intelligence)) {
            log.info("Preserving ${item.filePath} - Intelligence score: ${intelligence.overallScore}")
            return // Skip deletion
        }
        
        // Continue with normal cleanup logic
        deleteMedia(item)
    }
}
```

### 3. Understanding Intelligence Scores

The `MediaIntelligence` object contains:

```kotlin
data class MediaIntelligence(
    val tmdbRating: Double? = null,        // 0-10 scale
    val imdbRating: Double? = null,        // 0-10 scale
    val popularityScore: Double? = null,   // 0-100 scale
    val trendingScore: Double? = null,     // 0-100 scale
    val availabilityScore: Double? = null, // 0-100 scale (future)
    val collectibilityScore: Double? = null, // 0-100 scale
    val overallScore: Double = 0.0       // Weighted composite 0-100
)
```

### 4. Custom Preservation Logic

You can also implement custom rules:

```kotlin
fun shouldKeepMedia(item: LibraryItem, intelligence: MediaIntelligence): Boolean {
    // Keep all trending content
    if ((intelligence.trendingScore ?: 0.0) >= 50.0) {
        return true
    }
    
    // Keep award winners (from OMDb)
    if (omdbService.hasAwards(item)) {
        return true
    }
    
    // Keep highly rated classics
    if ((intelligence.imdbRating ?: 0.0) >= 7.5 && 
        (intelligence.collectibilityScore ?: 0.0) >= 60.0) {
        return true
    }
    
    // Use default preservation logic
    return externalDataService.shouldPreserveMedia(intelligence)
}
```

## Integration with Cleanup Schedules

### Media Deletion with Intelligence

```kotlin
@Scheduled(cron = "\${application.media-deletion.schedule}")
fun cleanupMedia() {
    val items = getAllLibraryItems()
    
    items.forEach { item ->
        // Get intelligence data
        val intelligence = externalDataService.enrichMediaData(item)
        
        // Log intelligence info
        log.debug("""
            Analyzing ${item.filePath}:
            - TMDB: ${intelligence.tmdbRating}
            - IMDb: ${intelligence.imdbRating}
            - Trending: ${intelligence.trendingScore}
            - Overall: ${intelligence.overallScore}
        """.trimIndent())
        
        // Check preservation
        if (externalDataService.shouldPreserveMedia(intelligence)) {
            log.info("PRESERVED: ${item.filePath} (score: ${intelligence.overallScore})")
            continue
        }
        
        // Normal cleanup logic
        if (shouldDeleteBasedOnAge(item)) {
            deleteMedia(item)
        }
    }
}
```

## Scoring Configuration

Adjust weights to match your preferences:

```yaml
external-apis:
  scoring:
    # Heavily favor ratings
    tmdb-rating-weight: 0.35
    imdb-rating-weight: 0.35
    
    # Less importance on trends
    popularity-weight: 0.10
    trending-weight: 0.10
    
    # Minimal consideration for availability/collectibility
    availability-weight: 0.05
    collectibility-weight: 0.05
```

## Performance Considerations

### Caching

API responses are cached for the configured interval:

```yaml
external-apis:
  cache-refresh-interval: 24h  # Refresh once per day
```

### Batch Processing

For large libraries, consider processing in batches:

```kotlin
fun cleanupLargeLibrary() {
    val items = getAllLibraryItems()
    
    items.chunked(100).forEach { batch ->
        batch.parallelStream().forEach { item ->
            val intelligence = externalDataService.enrichMediaData(item)
            processItem(item, intelligence)
        }
    }
}
```

## Troubleshooting

### Missing API Keys

If API keys are not configured, the service will gracefully degrade:

```kotlin
// Returns empty intelligence if APIs are disabled
val intelligence = externalDataService.enrichMediaData(item)
// intelligence.overallScore will be 0.0
```

### API Rate Limits

If you hit rate limits:
1. Increase `cache-refresh-interval`
2. Disable less critical APIs
3. Reduce cleanup frequency

### Debug Logging

Enable debug logging to see API calls:

```yaml
logging:
  level:
    com.github.schaka.janitorr.external: DEBUG
```

## Example Scenarios

### Scenario 1: Keep Oscar Winners

```kotlin
val intelligence = externalDataService.enrichMediaData(item)
if (omdbService.hasAwards(item)) {
    log.info("Keeping award winner: ${item.filePath}")
    return
}
```

### Scenario 2: Delete Low-Rated Old Content

```kotlin
val intelligence = externalDataService.enrichMediaData(item)
if ((intelligence.imdbRating ?: 10.0) < 6.0 && 
    item.historyAge.isBefore(LocalDateTime.now().minusMonths(3))) {
    log.info("Deleting low-rated old content: ${item.filePath}")
    deleteMedia(item)
}
```

### Scenario 3: Keep Trending Shows

```kotlin
val intelligence = externalDataService.enrichMediaData(item)
if ((intelligence.trendingScore ?: 0.0) >= 75.0) {
    log.info("Keeping trending content: ${item.filePath}")
    addToTrendingCollection(item)
    return
}
```

## Best Practices

1. **Always handle nulls**: API data may be unavailable
2. **Use graceful degradation**: Don't fail cleanup if APIs are down
3. **Log preservation decisions**: Track why content was kept
4. **Monitor API usage**: Watch for rate limit issues
5. **Test with dry-run**: Verify behavior before enabling deletions
6. **Adjust weights gradually**: Fine-tune scoring over time

## Next Steps

For more information:
- [Configuration Guide](Configuration-Guide.md)
- [Troubleshooting](Troubleshooting.md)
- [FAQ](FAQ.md)
