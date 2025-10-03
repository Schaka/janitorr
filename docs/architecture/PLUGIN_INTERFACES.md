# Plugin Interface Specifications

This document defines the core interfaces and data structures for the Janitorr Plugin System.

## Table of Contents

- [Base Plugin Interface](#base-plugin-interface)
- [Cleanup Plugin](#cleanup-plugin)
- [Notification Plugin](#notification-plugin)
- [Data Source Plugin](#data-source-plugin)
- [UI Plugin](#ui-plugin)
- [Plugin SDK](#plugin-sdk)
- [Data Structures](#data-structures)

## Base Plugin Interface

All plugin types extend this base interface.

```kotlin
package com.github.schaka.janitorr.plugin.api

/**
 * Base interface for all Janitorr plugins
 */
interface Plugin {
    
    /**
     * Called when the plugin is first loaded and initialized.
     * This is where the plugin should perform any setup operations.
     * 
     * @param sdk The Janitorr Plugin SDK providing access to core services
     */
    fun onInit(sdk: JanitorrPluginSDK)
    
    /**
     * Called when the plugin is being unloaded or the application is shutting down.
     * This is where the plugin should clean up resources, close connections, etc.
     */
    fun onDestroy()
    
    /**
     * Returns metadata about this plugin
     * 
     * @return Plugin metadata including name, version, author, etc.
     */
    fun getMetadata(): PluginMetadata
    
    /**
     * Optional: Called when plugin configuration is updated
     * Default implementation does nothing
     * 
     * @param config Updated configuration map
     */
    fun onConfigurationChange(config: Map<String, Any>) {
        // Default: no-op
    }
    
    /**
     * Optional: Performs a health check on the plugin
     * Default implementation returns healthy status
     * 
     * @return Health check result
     */
    fun healthCheck(): PluginHealthStatus {
        return PluginHealthStatus(
            healthy = true,
            message = "Plugin is operational"
        )
    }
}

/**
 * Plugin metadata
 */
data class PluginMetadata(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val website: String? = null,
    val license: String? = null,
    val apiVersion: String
)

/**
 * Plugin health status
 */
data class PluginHealthStatus(
    val healthy: Boolean,
    val message: String,
    val lastCheck: Long = System.currentTimeMillis()
)
```

## Cleanup Plugin

Extends media cleanup logic with custom rules and strategies.

```kotlin
package com.github.schaka.janitorr.plugin.api

/**
 * Plugin interface for custom cleanup logic
 */
interface CleanupPlugin : Plugin {
    
    /**
     * Determines if a media item should be deleted based on custom logic.
     * 
     * This method is called during cleanup runs to evaluate each media item.
     * The plugin can inspect the media item and context to make decisions.
     * 
     * @param media The media item being evaluated
     * @param context Additional context about the cleanup run
     * @return true if the media should be deleted, false otherwise
     */
    fun shouldDelete(media: MediaItem, context: CleanupContext): Boolean
    
    /**
     * Executes custom cleanup operations for a media item.
     * 
     * This method is called when a media item has been marked for deletion.
     * The plugin can perform additional actions like archiving, notification, etc.
     * 
     * @param media The media item being cleaned up
     * @return Result of the cleanup operation
     */
    fun executeCleanup(media: MediaItem): CleanupResult
    
    /**
     * Optional: Returns the priority of this cleanup plugin.
     * Higher priority plugins are evaluated first.
     * Default priority is 0.
     * 
     * @return Priority value (higher = evaluated first)
     */
    fun getPriority(): Int = 0
    
    /**
     * Optional: Returns custom cleanup criteria that can be used
     * to filter media items before evaluation.
     * 
     * @return Cleanup criteria or null for no pre-filtering
     */
    fun getCleanupCriteria(): CleanupCriteria? = null
}

/**
 * Represents a media item in the library
 */
data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val path: String,
    val addedDate: Long,
    val lastWatched: Long?,
    val watchCount: Int,
    val fileSize: Long,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Media type enumeration
 */
enum class MediaType {
    MOVIE,
    TV_SHOW,
    SEASON,
    EPISODE,
    UNKNOWN
}

/**
 * Context for cleanup operations
 */
data class CleanupContext(
    val dryRun: Boolean,
    val diskThreshold: Double?,
    val currentDiskUsage: Double,
    val minimumDays: Int,
    val exclusionTags: List<String>,
    val customParameters: Map<String, Any> = emptyMap()
)

/**
 * Result of a cleanup operation
 */
data class CleanupResult(
    val success: Boolean,
    val message: String,
    val deletedFiles: List<String> = emptyList(),
    val archivedFiles: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

/**
 * Custom cleanup criteria for pre-filtering
 */
data class CleanupCriteria(
    val minFileSize: Long? = null,
    val maxFileSize: Long? = null,
    val mediaTypes: List<MediaType>? = null,
    val minDaysOld: Int? = null,
    val requiresUnwatched: Boolean = false
)
```

## Notification Plugin

Send notifications through custom channels.

```kotlin
package com.github.schaka.janitorr.plugin.api

/**
 * Plugin interface for custom notification channels
 */
interface NotificationPlugin : Plugin {
    
    /**
     * Sends a notification event through this channel.
     * 
     * @param event The notification event to send
     * @return true if notification was sent successfully, false otherwise
     */
    fun sendNotification(event: NotificationEvent): Boolean
    
    /**
     * Configures the notification plugin with user settings.
     * 
     * This method is called when the user configures or updates
     * the plugin settings. The plugin should validate the settings
     * and store them for later use.
     * 
     * @param settings Configuration settings as key-value pairs
     * @return Validation result indicating success or errors
     */
    fun configure(settings: Map<String, Any>): ValidationResult
    
    /**
     * Tests the notification channel connection.
     * 
     * This method is called when the user wants to test if the
     * notification channel is properly configured and accessible.
     * 
     * @return Connection test result
     */
    fun testConnection(): ConnectionResult
    
    /**
     * Optional: Returns the supported event types for this channel.
     * If null, all event types are supported.
     * 
     * @return List of supported event types or null for all types
     */
    fun getSupportedEventTypes(): List<NotificationEventType>? = null
    
    /**
     * Optional: Returns whether this channel supports rich formatting
     * (HTML, Markdown, etc.)
     * 
     * @return true if rich formatting is supported
     */
    fun supportsRichFormatting(): Boolean = false
}

/**
 * Notification event types
 */
enum class NotificationEventType {
    CLEANUP_STARTED,
    CLEANUP_COMPLETED,
    CLEANUP_FAILED,
    MEDIA_DELETED,
    MEDIA_ARCHIVED,
    ERROR_OCCURRED,
    WARNING_OCCURRED,
    PLUGIN_INSTALLED,
    PLUGIN_ERROR
}

/**
 * Notification event
 */
data class NotificationEvent(
    val type: NotificationEventType,
    val title: String,
    val message: String,
    val severity: NotificationSeverity = NotificationSeverity.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Notification severity levels
 */
enum class NotificationSeverity {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

/**
 * Validation result
 */
data class ValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Connection test result
 */
data class ConnectionResult(
    val connected: Boolean,
    val message: String,
    val responseTime: Long? = null
)
```

## Data Source Plugin

Enrich media information from external sources.

```kotlin
package com.github.schaka.janitorr.plugin.api

/**
 * Plugin interface for external data sources
 */
interface DataSourcePlugin : Plugin {
    
    /**
     * Enriches a media item with additional data from this source.
     * 
     * The plugin can query external APIs, databases, or services
     * to gather additional information about the media item.
     * 
     * @param media The media item to enrich
     * @return Enriched media data or null if no data available
     */
    fun enrichMedia(media: MediaItem): EnrichedMediaData?
    
    /**
     * Checks if the data source is currently available.
     * 
     * This method should verify connectivity and service status.
     * 
     * @return true if the data source is available
     */
    fun isAvailable(): Boolean
    
    /**
     * Returns rate limiting information for this data source.
     * 
     * @return Rate limit information
     */
    fun getRateLimitInfo(): RateLimitInfo
    
    /**
     * Optional: Performs a batch enrichment operation.
     * More efficient than calling enrichMedia multiple times.
     * 
     * @param mediaItems List of media items to enrich
     * @return Map of media ID to enriched data
     */
    fun enrichMediaBatch(mediaItems: List<MediaItem>): Map<String, EnrichedMediaData> {
        return mediaItems.mapNotNull { media ->
            enrichMedia(media)?.let { media.id to it }
        }.toMap()
    }
    
    /**
     * Optional: Returns supported media types for this data source.
     * If null, all media types are supported.
     * 
     * @return List of supported media types or null for all types
     */
    fun getSupportedMediaTypes(): List<MediaType>? = null
}

/**
 * Enriched media data from external source
 */
data class EnrichedMediaData(
    val mediaId: String,
    val source: String,
    val ratings: Map<String, Double> = emptyMap(),
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val popularity: Double? = null,
    val trendingScore: Double? = null,
    val viewingStatistics: ViewingStatistics? = null,
    val customData: Map<String, Any> = emptyMap()
)

/**
 * Viewing statistics from external source
 */
data class ViewingStatistics(
    val totalViews: Long,
    val uniqueViewers: Long,
    val averageRating: Double?,
    val completionRate: Double?,
    val lastViewedDate: Long?
)

/**
 * Rate limiting information
 */
data class RateLimitInfo(
    val requestsPerMinute: Int,
    val requestsPerHour: Int?,
    val remainingRequests: Int?,
    val resetTime: Long?
)
```

## UI Plugin

Extend the Management UI with custom widgets and pages.

```kotlin
package com.github.schaka.janitorr.plugin.api

/**
 * Plugin interface for UI extensions
 */
interface UIPlugin : Plugin {
    
    /**
     * Returns custom dashboard widgets to display on the main dashboard.
     * 
     * @return List of dashboard widgets
     */
    fun getDashboardWidgets(): List<DashboardWidget>
    
    /**
     * Returns a configuration panel for this plugin's settings.
     * 
     * @return Configuration panel or null if no configuration needed
     */
    fun getConfigurationPanel(): ConfigurationPanel?
    
    /**
     * Returns custom pages to add to the navigation.
     * 
     * @return List of custom pages
     */
    fun getCustomPages(): List<CustomPage>
    
    /**
     * Optional: Returns custom CSS to inject into the UI.
     * 
     * @return CSS content or null
     */
    fun getCustomCSS(): String? = null
    
    /**
     * Optional: Returns custom JavaScript to inject into the UI.
     * 
     * @return JavaScript content or null
     */
    fun getCustomJavaScript(): String? = null
}

/**
 * Dashboard widget definition
 */
data class DashboardWidget(
    val id: String,
    val title: String,
    val htmlContent: String,
    val position: WidgetPosition = WidgetPosition.BOTTOM,
    val width: WidgetWidth = WidgetWidth.FULL,
    val refreshInterval: Int? = null // seconds
)

/**
 * Widget position on dashboard
 */
enum class WidgetPosition {
    TOP,
    MIDDLE,
    BOTTOM
}

/**
 * Widget width options
 */
enum class WidgetWidth {
    FULL,
    HALF,
    THIRD,
    QUARTER
}

/**
 * Configuration panel definition
 */
data class ConfigurationPanel(
    val title: String,
    val fields: List<ConfigurationField>
)

/**
 * Configuration field definition
 */
data class ConfigurationField(
    val id: String,
    val label: String,
    val type: FieldType,
    val defaultValue: Any?,
    val required: Boolean = false,
    val description: String? = null,
    val options: List<String>? = null, // For SELECT type
    val validation: FieldValidation? = null
)

/**
 * Field types for configuration
 */
enum class FieldType {
    TEXT,
    NUMBER,
    BOOLEAN,
    SELECT,
    TEXTAREA,
    PASSWORD,
    URL,
    EMAIL
}

/**
 * Field validation rules
 */
data class FieldValidation(
    val min: Double? = null,
    val max: Double? = null,
    val pattern: String? = null,
    val custom: ((Any) -> Boolean)? = null
)

/**
 * Custom page definition
 */
data class CustomPage(
    val id: String,
    val title: String,
    val path: String,
    val htmlContent: String,
    val icon: String? = null
)
```

## Plugin SDK

The SDK provided to all plugins for accessing Janitorr services.

```kotlin
package com.github.schaka.janitorr.plugin.sdk

/**
 * Main SDK entry point provided to all plugins
 */
interface JanitorrPluginSDK {
    
    /**
     * SDK version
     */
    val version: String
    
    /**
     * Service for accessing media library information
     */
    val mediaService: MediaService
    
    /**
     * Service for accessing and modifying configuration
     */
    val configService: ConfigService
    
    /**
     * Service for sending notifications
     */
    val notificationService: NotificationService
    
    /**
     * Plugin-specific logger
     */
    val logger: PluginLogger
    
    /**
     * Plugin-specific storage
     */
    val storage: PluginStorage
    
    /**
     * Task scheduler
     */
    val scheduler: PluginScheduler
}

/**
 * Media service interface
 */
interface MediaService {
    fun getMediaById(id: String): MediaItem?
    fun getAllMedia(type: MediaType? = null): List<MediaItem>
    fun getMediaByPath(path: String): MediaItem?
    fun searchMedia(query: String): List<MediaItem>
}

/**
 * Configuration service interface
 */
interface ConfigService {
    fun get(key: String): Any?
    fun set(key: String, value: Any)
    fun getPluginConfig(pluginId: String): Map<String, Any>
    fun setPluginConfig(pluginId: String, config: Map<String, Any>)
}

/**
 * Notification service interface
 */
interface NotificationService {
    fun sendNotification(event: NotificationEvent)
    fun sendToChannel(channelId: String, event: NotificationEvent): Boolean
}

/**
 * Plugin logger interface
 */
interface PluginLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * Plugin storage interface for persisting data
 */
interface PluginStorage {
    fun get(key: String): String?
    fun set(key: String, value: String)
    fun remove(key: String)
    fun clear()
    fun keys(): Set<String>
}

/**
 * Plugin scheduler interface for scheduling tasks
 */
interface PluginScheduler {
    fun scheduleTask(task: Runnable, delaySeconds: Long)
    fun scheduleRepeatingTask(task: Runnable, intervalSeconds: Long)
    fun cancelAllTasks()
}
```

## Data Structures

Common data structures used across plugin interfaces.

```kotlin
package com.github.schaka.janitorr.plugin.api

/**
 * Plugin state enumeration
 */
enum class PluginState {
    DISCOVERED,
    VALIDATED,
    LOADED,
    CONFIGURED,
    INITIALIZED,
    ACTIVE,
    DISABLED,
    FAILED,
    UNLOADED
}

/**
 * Plugin information for registry
 */
data class PluginInfo(
    val id: String,
    val metadata: PluginMetadata,
    val state: PluginState,
    val enabled: Boolean,
    val loadedAt: Long?,
    val errors: List<String> = emptyList()
)

/**
 * Plugin execution context
 */
data class PluginContext(
    val pluginId: String,
    val permissions: Set<String>,
    val resourceLimits: ResourceLimits,
    val configuration: Map<String, Any>
)

/**
 * Resource limits for plugin execution
 */
data class ResourceLimits(
    val maxCpuPercent: Int = 10,
    val maxThreads: Int = 5,
    val maxHeapMb: Long = 128,
    val maxDirectMb: Long = 32,
    val maxStorageMb: Long = 100,
    val maxRequestsPerMinute: Int = 60
)

/**
 * Plugin metrics for monitoring
 */
data class PluginMetrics(
    val pluginId: String,
    val state: PluginState,
    val uptime: Long,
    val cpuUsage: Double,
    val memoryUsage: Long,
    val apiCallCount: Long,
    val errorCount: Long,
    val lastError: String?,
    val lastErrorTime: Long?
)
```

## Example Implementations

### Example Cleanup Plugin

```kotlin
package com.example.plugins

import com.github.schaka.janitorr.plugin.api.*
import com.github.schaka.janitorr.plugin.sdk.JanitorrPluginSDK

/**
 * Example cleanup plugin that deletes media based on file size
 */
class FileSizeCleanupPlugin : CleanupPlugin {
    
    private lateinit var sdk: JanitorrPluginSDK
    private var maxFileSizeMb: Long = 10000 // 10GB default
    
    override fun onInit(sdk: JanitorrPluginSDK) {
        this.sdk = sdk
        sdk.logger.info("FileSizeCleanupPlugin initialized")
        
        // Load configuration
        val config = sdk.configService.getPluginConfig("file-size-cleanup")
        maxFileSizeMb = (config["maxFileSizeMb"] as? Number)?.toLong() ?: 10000
    }
    
    override fun onDestroy() {
        sdk.logger.info("FileSizeCleanupPlugin destroyed")
    }
    
    override fun getMetadata(): PluginMetadata {
        return PluginMetadata(
            name = "File Size Cleanup",
            version = "1.0.0",
            author = "Example Author",
            description = "Deletes media files exceeding maximum size",
            apiVersion = "2.0"
        )
    }
    
    override fun shouldDelete(media: MediaItem, context: CleanupContext): Boolean {
        val fileSizeMb = media.fileSize / 1024 / 1024
        return fileSizeMb > maxFileSizeMb
    }
    
    override fun executeCleanup(media: MediaItem): CleanupResult {
        sdk.logger.info("Cleaning up oversized file: ${media.title}")
        
        return CleanupResult(
            success = true,
            message = "File marked for deletion due to size",
            deletedFiles = listOf(media.path)
        )
    }
    
    override fun getPriority(): Int = 10
}
```

### Example Notification Plugin

```kotlin
package com.example.plugins

import com.github.schaka.janitorr.plugin.api.*
import com.github.schaka.janitorr.plugin.sdk.JanitorrPluginSDK

/**
 * Example notification plugin for Slack
 */
class SlackNotificationPlugin : NotificationPlugin {
    
    private lateinit var sdk: JanitorrPluginSDK
    private var webhookUrl: String = ""
    
    override fun onInit(sdk: JanitorrPluginSDK) {
        this.sdk = sdk
        sdk.logger.info("SlackNotificationPlugin initialized")
    }
    
    override fun onDestroy() {
        sdk.logger.info("SlackNotificationPlugin destroyed")
    }
    
    override fun getMetadata(): PluginMetadata {
        return PluginMetadata(
            name = "Slack Notifications",
            version = "1.0.0",
            author = "Example Author",
            description = "Send notifications to Slack",
            website = "https://example.com",
            apiVersion = "2.0"
        )
    }
    
    override fun sendNotification(event: NotificationEvent): Boolean {
        if (webhookUrl.isEmpty()) {
            sdk.logger.error("Webhook URL not configured")
            return false
        }
        
        try {
            // Send notification to Slack
            sdk.logger.info("Sending notification to Slack: ${event.title}")
            // Implementation would use HTTP client here
            return true
        } catch (e: Exception) {
            sdk.logger.error("Failed to send Slack notification", e)
            return false
        }
    }
    
    override fun configure(settings: Map<String, Any>): ValidationResult {
        val url = settings["webhookUrl"] as? String
        
        return if (url.isNullOrEmpty()) {
            ValidationResult(
                valid = false,
                errors = listOf("Webhook URL is required")
            )
        } else {
            webhookUrl = url
            ValidationResult(valid = true)
        }
    }
    
    override fun testConnection(): ConnectionResult {
        return try {
            // Test connection to Slack
            ConnectionResult(
                connected = true,
                message = "Successfully connected to Slack"
            )
        } catch (e: Exception) {
            ConnectionResult(
                connected = false,
                message = "Failed to connect: ${e.message}"
            )
        }
    }
    
    override fun supportsRichFormatting(): Boolean = true
}
```

## Version Compatibility

Plugin API versions follow semantic versioning:

- **Major version**: Breaking changes to interfaces
- **Minor version**: New features, backward compatible
- **Patch version**: Bug fixes, backward compatible

Plugins declare their required API version in `plugin.yml`:

```yaml
apiVersion: "2.0"  # Requires exactly 2.x
```

Version compatibility matrix:

| Plugin API Version | Compatible Janitorr Versions |
|-------------------|------------------------------|
| 1.0               | 1.x                          |
| 2.0               | 2.x                          |
| 2.1               | 2.1+                         |

## Best Practices

1. **Error Handling**: Always handle exceptions and return meaningful error messages
2. **Resource Cleanup**: Properly clean up resources in `onDestroy()`
3. **Logging**: Use the provided logger for all logging operations
4. **Configuration**: Validate all configuration values in `configure()`
5. **Permissions**: Request minimum required permissions
6. **Performance**: Avoid blocking operations in plugin methods
7. **Testing**: Thoroughly test plugin with various inputs and edge cases
