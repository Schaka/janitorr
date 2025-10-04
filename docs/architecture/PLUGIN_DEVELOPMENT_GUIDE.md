# Plugin Development Guide

This guide walks you through creating a custom plugin for Janitorr.

## Table of Contents

- [Getting Started](#getting-started)
- [Plugin Project Setup](#plugin-project-setup)
- [Implementing Your Plugin](#implementing-your-plugin)
- [Testing Your Plugin](#testing-your-plugin)
- [Packaging and Distribution](#packaging-and-distribution)
- [Publishing to Marketplace](#publishing-to-marketplace)

## Getting Started

### Prerequisites

- JDK 24 or newer
- Gradle 8.x or newer
- Basic knowledge of Kotlin
- Familiarity with Janitorr configuration

### Choose Your Plugin Type

Janitorr supports four main plugin types:

1. **Cleanup Plugin**: Custom media cleanup logic
2. **Notification Plugin**: Custom notification channels
3. **Data Source Plugin**: External data integration
4. **UI Plugin**: UI extensions and widgets

## Plugin Project Setup

### Manual Setup

Create a new Gradle project with this structure:

```
my-plugin/
├── build.gradle.kts
├── settings.gradle.kts
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── com/example/
│       │       └── MyPlugin.kt
│       └── resources/
│           └── plugin.yml
```

### build.gradle.kts

```kotlin
plugins {
    kotlin("jvm") version "2.2.20"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    // Janitorr plugin SDK repository (when available)
    maven("https://maven.janitorr.app/releases")
}

dependencies {
    // Janitorr Plugin SDK (when available)
    compileOnly("com.github.schaka.janitorr:plugin-sdk:2.0.0")
    
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}

tasks.jar {
    manifest {
        attributes(
            "Plugin-Class" to "com.example.MyPlugin"
        )
    }
}
```

### settings.gradle.kts

```kotlin
rootProject.name = "my-plugin"
```

## Implementing Your Plugin

### Example: Simple Cleanup Plugin

Create `src/main/kotlin/com/example/DuplicateCleanupPlugin.kt`:

```kotlin
package com.example

import com.github.schaka.janitorr.plugin.api.*
import com.github.schaka.janitorr.plugin.sdk.JanitorrPluginSDK

/**
 * Plugin that identifies and cleans up duplicate media files
 */
class DuplicateCleanupPlugin : CleanupPlugin {
    
    private lateinit var sdk: JanitorrPluginSDK
    private var similarityThreshold: Double = 0.85
    
    override fun onInit(sdk: JanitorrPluginSDK) {
        this.sdk = sdk
        
        // Load plugin configuration
        val config = sdk.configService.getPluginConfig("duplicate-cleanup")
        similarityThreshold = (config["similarityThreshold"] as? Number)?.toDouble() ?: 0.85
        
        sdk.logger.info("DuplicateCleanupPlugin initialized with threshold: $similarityThreshold")
    }
    
    override fun onDestroy() {
        sdk.logger.info("DuplicateCleanupPlugin shutting down")
    }
    
    override fun getMetadata(): PluginMetadata {
        return PluginMetadata(
            name = "Duplicate Cleanup",
            version = "1.0.0",
            author = "Your Name",
            description = "Identifies and removes duplicate media files",
            website = "https://github.com/yourusername/duplicate-cleanup-plugin",
            license = "Apache-2.0",
            apiVersion = "2.0"
        )
    }
    
    override fun shouldDelete(media: MediaItem, context: CleanupContext): Boolean {
        // Skip if in dry-run mode
        if (context.dryRun) {
            sdk.logger.debug("Dry-run mode: checking ${media.title}")
        }
        
        // Get all media of the same type
        val allMedia = sdk.mediaService.getAllMedia(media.type)
        
        // Find potential duplicates
        val duplicates = allMedia.filter { otherMedia ->
            otherMedia.id != media.id && 
            areSimilar(media, otherMedia)
        }
        
        if (duplicates.isEmpty()) {
            return false
        }
        
        sdk.logger.info("Found ${duplicates.size} potential duplicates for: ${media.title}")
        
        // Keep the highest quality version
        val keepMedia = (duplicates + media).maxByOrNull { it.fileSize } ?: media
        
        return keepMedia.id != media.id
    }
    
    override fun executeCleanup(media: MediaItem): CleanupResult {
        sdk.logger.info("Cleaning up duplicate: ${media.title}")
        
        try {
            // Send notification about the deletion
            sdk.notificationService.sendNotification(
                NotificationEvent(
                    type = NotificationEventType.MEDIA_DELETED,
                    title = "Duplicate Removed",
                    message = "Removed duplicate: ${media.title}",
                    severity = NotificationSeverity.INFO
                )
            )
            
            return CleanupResult(
                success = true,
                message = "Successfully removed duplicate",
                deletedFiles = listOf(media.path)
            )
        } catch (e: Exception) {
            sdk.logger.error("Failed to cleanup duplicate", e)
            return CleanupResult(
                success = false,
                message = "Failed: ${e.message}",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }
    
    override fun getPriority(): Int = 5
    
    private fun areSimilar(media1: MediaItem, media2: MediaItem): Boolean {
        // Simple title-based similarity
        val title1 = normalizeTitle(media1.title)
        val title2 = normalizeTitle(media2.title)
        
        val similarity = calculateSimilarity(title1, title2)
        return similarity >= similarityThreshold
    }
    
    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
    }
    
    private fun calculateSimilarity(str1: String, str2: String): Double {
        // Levenshtein distance-based similarity
        val longer = if (str1.length > str2.length) str1 else str2
        val shorter = if (str1.length > str2.length) str2 else str1
        
        if (longer.isEmpty()) return 1.0
        
        val distance = levenshteinDistance(longer, shorter)
        return (longer.length - distance).toDouble() / longer.length
    }
    
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val costs = IntArray(str2.length + 1)
        
        for (i in costs.indices) {
            costs[i] = i
        }
        
        for (i in 1..str1.length) {
            var lastValue = i
            for (j in 1..str2.length) {
                val newValue = if (str1[i - 1] == str2[j - 1]) {
                    costs[j - 1]
                } else {
                    minOf(costs[j - 1], costs[j], lastValue) + 1
                }
                costs[j - 1] = lastValue
                lastValue = newValue
            }
            costs[str2.length] = lastValue
        }
        
        return costs[str2.length]
    }
}
```

### Plugin Manifest

Create `src/main/resources/plugin.yml`:

```yaml
# Plugin metadata
name: "duplicate-cleanup"
version: "1.0.0"
apiVersion: "2.0"
main: "com.example.DuplicateCleanupPlugin"

# Author information
author: "Your Name"
description: "Identifies and removes duplicate media files based on similarity"
website: "https://github.com/yourusername/duplicate-cleanup-plugin"
license: "Apache-2.0"

# Plugin type
types:
  - "cleanup"

# Dependencies
dependencies:
  janitorr-core:
    version: ">=2.0.0"

# Required permissions
permissions:
  - "api.media.read"
  - "api.media.write"
  - "api.notification.send"

# Resource limits
resources:
  cpu:
    maxCpuPercent: 10
    maxThreads: 3
  memory:
    maxHeapMb: 64
  network:
    maxRequestsPerMinute: 0  # No network access needed

# Configuration schema
configuration:
  similarityThreshold:
    type: "number"
    default: 0.85
    min: 0.0
    max: 1.0
    description: "Similarity threshold for duplicate detection (0.0-1.0)"
  
  preferHigherQuality:
    type: "boolean"
    default: true
    description: "Keep the file with larger size (assumed higher quality)"
```

## Testing Your Plugin

### Unit Tests

Create `src/test/kotlin/com/example/DuplicateCleanupPluginTest.kt`:

```kotlin
package com.example

import com.github.schaka.janitorr.plugin.api.*
import com.github.schaka.janitorr.plugin.sdk.*
import io.mockk.*
import kotlin.test.*

class DuplicateCleanupPluginTest {
    
    private lateinit var plugin: DuplicateCleanupPlugin
    private lateinit var mockSdk: JanitorrPluginSDK
    private lateinit var mockMediaService: MediaService
    private lateinit var mockConfigService: ConfigService
    private lateinit var mockLogger: PluginLogger
    
    @BeforeTest
    fun setup() {
        mockSdk = mockk(relaxed = true)
        mockMediaService = mockk(relaxed = true)
        mockConfigService = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        
        every { mockSdk.mediaService } returns mockMediaService
        every { mockSdk.configService } returns mockConfigService
        every { mockSdk.logger } returns mockLogger
        every { mockConfigService.getPluginConfig(any()) } returns emptyMap()
        
        plugin = DuplicateCleanupPlugin()
        plugin.onInit(mockSdk)
    }
    
    @Test
    fun testMetadata() {
        val metadata = plugin.getMetadata()
        
        assertEquals("Duplicate Cleanup", metadata.name)
        assertEquals("1.0.0", metadata.version)
        assertEquals("2.0", metadata.apiVersion)
    }
    
    @Test
    fun testNoDuplicatesFound() {
        val media = MediaItem(
            id = "1",
            title = "Unique Movie",
            type = MediaType.MOVIE,
            path = "/data/movies/unique.mkv",
            addedDate = System.currentTimeMillis(),
            lastWatched = null,
            watchCount = 0,
            fileSize = 1000000
        )
        
        every { mockMediaService.getAllMedia(MediaType.MOVIE) } returns listOf(media)
        
        val context = CleanupContext(
            dryRun = false,
            diskThreshold = null,
            currentDiskUsage = 50.0,
            minimumDays = 30,
            exclusionTags = emptyList()
        )
        
        assertFalse(plugin.shouldDelete(media, context))
    }
    
    @Test
    fun testDuplicatesDetected() {
        val media1 = MediaItem(
            id = "1",
            title = "The Matrix",
            type = MediaType.MOVIE,
            path = "/data/movies/matrix1.mkv",
            addedDate = System.currentTimeMillis(),
            lastWatched = null,
            watchCount = 0,
            fileSize = 1000000
        )
        
        val media2 = MediaItem(
            id = "2",
            title = "The Matrix",
            type = MediaType.MOVIE,
            path = "/data/movies/matrix2.mkv",
            addedDate = System.currentTimeMillis(),
            lastWatched = null,
            watchCount = 0,
            fileSize = 2000000  // Larger file
        )
        
        every { mockMediaService.getAllMedia(MediaType.MOVIE) } returns listOf(media1, media2)
        
        val context = CleanupContext(
            dryRun = false,
            diskThreshold = null,
            currentDiskUsage = 50.0,
            minimumDays = 30,
            exclusionTags = emptyList()
        )
        
        // Smaller file should be deleted
        assertTrue(plugin.shouldDelete(media1, context))
        // Larger file should be kept
        assertFalse(plugin.shouldDelete(media2, context))
    }
}
```

### Running Tests

```bash
./gradlew test
```

### Local Testing

To test your plugin with a running Janitorr instance:

1. Build the plugin JAR:
   ```bash
   ./gradlew jar
   ```

2. Copy the JAR to Janitorr's plugin directory:
   ```bash
   cp build/libs/my-plugin-1.0.0.jar /path/to/janitorr/config/plugins/enabled/
   ```

3. Restart Janitorr or use hot-reload (if enabled):
   ```bash
   docker-compose restart janitorr
   ```

4. Check logs for plugin initialization:
   ```bash
   docker logs janitorr | grep "DuplicateCleanupPlugin"
   ```

## Packaging and Distribution

### Build the Plugin

```bash
./gradlew clean build
```

The plugin JAR will be created in `build/libs/`.

### Plugin JAR Structure

```
my-plugin-1.0.0.jar
├── META-INF/
│   └── MANIFEST.MF
├── plugin.yml
└── com/example/
    └── DuplicateCleanupPlugin.class
```

### Create Documentation

Include a `README.md` in your plugin repository:

```markdown
# Duplicate Cleanup Plugin for Janitorr

## Description
Identifies and removes duplicate media files based on title similarity.

## Installation

1. Download the latest release JAR
2. Copy to `/config/plugins/enabled/` in your Janitorr installation
3. Restart Janitorr
4. Configure the plugin in the Janitorr UI

## Configuration

- `similarityThreshold`: Similarity threshold (0.0-1.0, default: 0.85)
- `preferHigherQuality`: Keep larger files (default: true)

## Permissions

- `api.media.read`: Read media library
- `api.media.write`: Delete duplicate files
- `api.notification.send`: Send notifications

## License

Apache-2.0
```

## Publishing to Marketplace

### Prerequisites

1. Create an account on Janitorr Marketplace
2. Generate an API key from your account settings

### Validation

Before publishing, validate your plugin:

```bash
janitorr-pdk validate build/libs/my-plugin-1.0.0.jar
```

This checks:
- Plugin manifest validity
- API version compatibility
- Required permissions
- Resource limits
- Code signing (if enabled)

### Publishing

```bash
janitorr-pdk publish build/libs/my-plugin-1.0.0.jar \
  --api-key YOUR_API_KEY \
  --description "Identifies and removes duplicate media files" \
  --tags "cleanup,duplicates,automation" \
  --category "cleanup"
```

### Marketplace Categories

- **Cleanup Extensions**: Media cleanup plugins
- **Notification Channels**: Notification integrations
- **Data Sources**: External data providers
- **UI Extensions**: UI widgets and pages
- **Utility Plugins**: General utilities

### Update Publishing

To publish an update:

1. Update version in `build.gradle.kts` and `plugin.yml`
2. Build the new JAR
3. Publish with the same command

Users will be notified of available updates.

## Best Practices

### Security

1. **Minimize Permissions**: Request only necessary permissions
2. **Validate Input**: Always validate user configuration
3. **Handle Errors**: Catch and handle all exceptions
4. **Avoid Secrets**: Never hardcode API keys or passwords

### Performance

1. **Resource Limits**: Stay within declared resource limits
2. **Async Operations**: Use async for long-running operations
3. **Caching**: Cache frequently accessed data
4. **Batch Operations**: Use batch APIs when available

### Logging

1. **Use SDK Logger**: Always use the provided logger
2. **Log Levels**: Use appropriate log levels (DEBUG, INFO, WARN, ERROR)
3. **Structured Logs**: Include context in log messages
4. **Avoid Spam**: Don't log excessively in loops

### Configuration

1. **Sensible Defaults**: Provide good default values
2. **Validation**: Validate all configuration values
3. **Documentation**: Document all configuration options
4. **Backward Compatibility**: Maintain config compatibility across versions

### Testing

1. **Unit Tests**: Write comprehensive unit tests
2. **Integration Tests**: Test with mock SDK
3. **Edge Cases**: Test boundary conditions
4. **Error Scenarios**: Test error handling

## Common Issues

### Plugin Not Loading

**Check:**
- Plugin JAR is in `/config/plugins/enabled/`
- `plugin.yml` is present and valid
- API version is compatible
- No syntax errors in plugin code
- Check Janitorr logs for errors

### Permission Denied Errors

**Check:**
- Required permissions are declared in `plugin.yml`
- Permissions match the operations you're trying to perform
- User has approved plugin permissions (if required)

### Resource Limit Errors

**Check:**
- Plugin is staying within declared resource limits
- Increase limits in `plugin.yml` if needed
- Optimize plugin code to reduce resource usage

## Support

- Documentation: https://docs.janitorr.app/plugins
- Forums: https://community.janitorr.app
- GitHub: https://github.com/schaka/janitorr/discussions
- Marketplace: https://marketplace.janitorr.app

## Example Plugins

Check out these example plugins for reference:

- **Trakt Integration**: Data source plugin example
- **Discord Notifier**: Notification plugin example
- **Advanced Analytics**: UI plugin example
- **Smart Archiver**: Cleanup plugin example

All examples available at: https://github.com/janitorr-plugins
