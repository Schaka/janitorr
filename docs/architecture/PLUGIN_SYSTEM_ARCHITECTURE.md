# Plugin System Architecture

## Overview

The Janitorr Plugin System is designed to provide a comprehensive, secure, and extensible framework that allows developers to create custom extensions for Janitorr. This architecture document outlines the design principles, components, and implementation strategy for the plugin system.

## Design Principles

1. **Security First**: All plugins run in isolated sandboxes with granular permissions
2. **Developer-Friendly**: Simple SDK with clear interfaces and comprehensive documentation
3. **Backward Compatible**: Existing functionality remains unchanged
4. **Performance**: Minimal overhead from plugin system
5. **Reliability**: Plugin failures should not crash the main application

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Janitorr Core                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │           Plugin Management Layer                      │ │
│  │                                                        │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │ │
│  │  │   Plugin     │  │   Plugin     │  │   Plugin    │ │ │
│  │  │   Loader     │  │   Registry   │  │   Lifecycle │ │ │
│  │  └──────────────┘  └──────────────┘  └─────────────┘ │ │
│  │                                                        │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │ │
│  │  │   Security   │  │  Validation  │  │   Config    │ │ │
│  │  │   Manager    │  │   Framework  │  │   Manager   │ │ │
│  │  └──────────────┘  └──────────────┘  └─────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │           Plugin Execution Environment                │ │
│  │                                                        │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │ │
│  │  │  Cleanup    │  │Notification │  │ Data Source  │  │ │
│  │  │  Plugins    │  │  Plugins    │  │   Plugins    │  │ │
│  │  └─────────────┘  └─────────────┘  └──────────────┘  │ │
│  │                                                        │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │ │
│  │  │ UI Plugins  │  │  Utility    │  │   Custom     │  │ │
│  │  │             │  │  Plugins    │  │   Plugins    │  │ │
│  │  └─────────────┘  └─────────────┘  └──────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Plugin SDK & API Layer                   │ │
│  │                                                        │ │
│  │  - Media Service API                                  │ │
│  │  - Configuration API                                  │ │
│  │  - Notification API                                   │ │
│  │  - Storage API                                        │ │
│  │  - Logging API                                        │ │
│  │  - Scheduling API                                     │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Plugin Types

### 1. Cleanup Plugins

Extend media cleanup logic with custom rules and strategies.

**Interface:**
```kotlin
interface CleanupPlugin : Plugin {
    /**
     * Determines if a media item should be deleted based on custom logic
     */
    fun shouldDelete(media: MediaItem, context: CleanupContext): Boolean
    
    /**
     * Executes custom cleanup operations
     */
    fun executeCleanup(media: MediaItem): CleanupResult
    
    /**
     * Returns plugin metadata
     */
    fun getMetadata(): PluginMetadata
}
```

**Use Cases:**
- AI-based content scoring
- Advanced duplicate detection
- Torrent seed ratio checking
- Cloud storage archiving

### 2. Notification Plugins

Send notifications through custom channels.

**Interface:**
```kotlin
interface NotificationPlugin : Plugin {
    /**
     * Sends a notification event
     */
    fun sendNotification(event: NotificationEvent): Boolean
    
    /**
     * Configures the notification plugin
     */
    fun configure(settings: Map<String, Any>): ValidationResult
    
    /**
     * Tests the notification channel connection
     */
    fun testConnection(): ConnectionResult
}
```

**Use Cases:**
- Slack integration
- Microsoft Teams notifications
- WhatsApp Business API
- Custom webhook builders

### 3. Data Source Plugins

Enrich media information from external sources.

**Interface:**
```kotlin
interface DataSourcePlugin : Plugin {
    /**
     * Enriches media item with additional data
     */
    fun enrichMedia(media: MediaItem): EnrichedMediaData
    
    /**
     * Checks if the data source is available
     */
    fun isAvailable(): Boolean
    
    /**
     * Returns rate limiting information
     */
    fun getRateLimitInfo(): RateLimitInfo
}
```

**Use Cases:**
- Trakt.tv integration
- Plex statistics
- Emby analytics
- Custom metadata providers

### 4. UI Plugins

Extend the Management UI with custom widgets and pages.

**Interface:**
```kotlin
interface UIPlugin : Plugin {
    /**
     * Returns custom dashboard widgets
     */
    fun getDashboardWidgets(): List<DashboardWidget>
    
    /**
     * Returns configuration panel
     */
    fun getConfigurationPanel(): ConfigurationPanel
    
    /**
     * Returns custom pages
     */
    fun getCustomPages(): List<CustomPage>
}
```

**Use Cases:**
- Advanced analytics dashboards
- Custom charts
- Mobile companion interfaces
- Theme extensions

## Plugin Lifecycle

```
┌─────────────┐
│ DISCOVERED  │ ──> Plugin files found in plugin directory
└──────┬──────┘
       │
       v
┌─────────────┐
│  VALIDATED  │ ──> Signature verified, manifest parsed
└──────┬──────┘
       │
       v
┌─────────────┐
│   LOADED    │ ──> Classes loaded, dependencies resolved
└──────┬──────┘
       │
       v
┌─────────────┐
│ CONFIGURED  │ ──> Configuration applied from settings
└──────┬──────┘
       │
       v
┌─────────────┐
│ INITIALIZED │ ──> Plugin.onInit() called
└──────┬──────┘
       │
       v
┌─────────────┐
│   ACTIVE    │ ──> Plugin operational, accepting calls
└──────┬──────┘
       │
       ├──> (hot reload) ──> RELOADING ──> back to VALIDATED
       │
       v
┌─────────────┐
│  DISABLED   │ ──> Plugin disabled but not unloaded
└──────┬──────┘
       │
       v
┌─────────────┐
│  UNLOADED   │ ──> Plugin.onDestroy() called, resources released
└─────────────┘
```

## Security & Sandboxing

### ClassLoader Isolation

Each plugin runs in its own isolated ClassLoader to prevent:
- Namespace collisions
- Version conflicts
- Memory leaks
- Unauthorized access to core classes

### Permission System

Plugins must declare required permissions in their manifest:

```yaml
permissions:
  - "filesystem.read:/data"
  - "filesystem.write:/data/temp"
  - "network.http:api.example.com"
  - "network.https:*"
  - "api.media.read"
  - "api.media.write"
```

Permission levels:
- **NONE**: No permissions (safe, read-only operations)
- **READ**: Read-only access to specified resources
- **WRITE**: Write access to specified resources
- **ADMIN**: Full administrative access (requires user confirmation)

### Resource Limits

Each plugin has configurable resource quotas:

```yaml
resources:
  cpu:
    maxCpuPercent: 10
    maxThreads: 5
  memory:
    maxHeapMb: 128
    maxDirectMb: 32
  disk:
    maxStorageMb: 100
  network:
    maxRequestsPerMinute: 60
    maxBandwidthKbps: 1024
```

### API Whitelist

Plugins can only access approved API classes and methods:
- JanitorrPluginSDK (full access)
- Kotlin stdlib (whitelisted subset)
- Java stdlib (whitelisted subset)
- Declared dependencies (specific versions)

## Plugin Manifest Specification

```yaml
# plugin.yml - Located at root of plugin JAR

# Required fields
name: "advanced-duplicate-finder"
version: "1.2.0"
apiVersion: "2.0"
main: "com.example.DuplicateFinderPlugin"

# Metadata
author: "CommunityDev"
description: "AI-powered duplicate detection with fuzzy matching"
website: "https://github.com/user/janitorr-duplicates"
license: "Apache-2.0"

# Plugin type (one or more)
types:
  - "cleanup"

# Dependencies
dependencies:
  janitorr-core:
    version: ">=2.0.0"
  apache-commons:
    version: "^3.12.0"

# Permissions
permissions:
  - "filesystem.read:/data/media"
  - "network.http:api.themoviedb.org"
  - "api.media.read"

# Resource limits (optional, uses defaults if not specified)
resources:
  cpu:
    maxCpuPercent: 5
  memory:
    maxHeapMb: 64

# Configuration schema
configuration:
  fuzzy_threshold:
    type: "number"
    default: 0.85
    min: 0.0
    max: 1.0
    description: "Similarity threshold for duplicate detection"
  
  enabled_sources:
    type: "array"
    items: "string"
    default: ["tmdb", "imdb"]
    description: "Data sources to use for comparison"
  
  auto_delete:
    type: "boolean"
    default: false
    description: "Automatically delete duplicates without confirmation"
```

## Plugin Discovery

### Plugin Directory Structure

```
/config/plugins/
├── enabled/
│   ├── advanced-duplicates-1.2.0.jar
│   ├── trakt-integration-2.1.1.jar
│   └── discord-notifier-1.0.5.jar
├── disabled/
│   └── old-plugin-0.9.0.jar
└── downloaded/
    └── new-plugin-1.0.0.jar (awaiting installation)
```

### Discovery Process

1. **Scan**: On startup, scan `/config/plugins/enabled/` for JAR files
2. **Parse**: Extract and parse `plugin.yml` from each JAR
3. **Validate**: Verify signature, check API version compatibility
4. **Register**: Add plugin to registry with metadata
5. **Load**: Load plugin classes if validation successful

## Plugin SDK

### Core SDK Classes

```kotlin
package com.github.schaka.janitorr.plugin.sdk

/**
 * Main SDK entry point provided to all plugins
 */
class JanitorrPluginSDK(
    val version: String,
    val mediaService: MediaService,
    val configService: ConfigService,
    val notificationService: NotificationService,
    val logger: PluginLogger,
    val storage: PluginStorage,
    val scheduler: PluginScheduler
)

/**
 * Base plugin interface
 */
interface Plugin {
    /**
     * Called when plugin is first loaded
     */
    fun onInit(sdk: JanitorrPluginSDK)
    
    /**
     * Called when plugin is being unloaded
     */
    fun onDestroy()
    
    /**
     * Returns plugin metadata
     */
    fun getMetadata(): PluginMetadata
}

/**
 * Plugin metadata
 */
data class PluginMetadata(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val website: String?
)
```

### Helper Utilities

```kotlin
package com.github.schaka.janitorr.plugin.sdk.util

object PluginUtils {
    /**
     * Parse plugin configuration from YAML/JSON
     */
    fun parseConfiguration(config: String, format: ConfigFormat): PluginConfig
    
    /**
     * Validate media path
     */
    fun validateMediaPath(path: String): Boolean
    
    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String
    
    /**
     * Schedule recurring task
     */
    fun scheduleTask(task: Runnable, interval: Duration)
    
    /**
     * Execute HTTP request with rate limiting
     */
    fun httpRequest(request: HttpRequest): HttpResponse
}
```

## Plugin Marketplace

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│               Plugin Marketplace Backend                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Plugin     │  │   Version    │  │  Download    │ │
│  │  Repository  │  │   Registry   │  │   Service    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Publishing  │  │  Validation  │  │ Statistics   │ │
│  │   Pipeline   │  │   Service    │  │   Service    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Review     │  │    Rating    │  │  Moderation  │ │
│  │   Service    │  │   Service    │  │   Service    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Marketplace Features

1. **Browse & Search**
   - Category-based navigation
   - Full-text search
   - Tag filtering
   - Popularity sorting
   - Recent uploads

2. **One-Click Install**
   - Download plugin JAR
   - Verify signature
   - Check dependencies
   - Install to plugin directory
   - Auto-configure with defaults

3. **Auto-Updates**
   - Check for updates on schedule
   - Notify user of available updates
   - One-click update installation
   - Automatic rollback on failure

4. **Rating System**
   - 5-star ratings
   - Written reviews
   - Usage statistics
   - Compatibility reports

5. **Dependency Resolution**
   - Parse dependency tree
   - Download required dependencies
   - Verify version compatibility
   - Install in correct order

6. **Version Management**
   - Multiple versions available
   - Version pinning
   - Rollback to previous version
   - Version compatibility matrix

### Publishing Workflow

```
Developer
   │
   │ 1. Create plugin using PDK
   ↓
Local Development
   │
   │ 2. Test plugin locally
   ↓
Plugin Validator
   │
   │ 3. Run pre-submission checks
   ↓
Submit to Marketplace
   │
   │ 4. Upload JAR + documentation
   ↓
Automated Validation
   │
   │ 5. Security scan, API compatibility
   ↓
Beta Testing Period
   │
   │ 6. Community testing (optional)
   ↓
Moderation Review
   │
   │ 7. Manual review for public plugins
   ↓
Published to Marketplace
   │
   │ 8. Available for installation
   ↓
Maintenance & Updates
```

## Plugin Development Kit (PDK)

### CLI Tool: janitorr-pdk

```bash
# Install PDK
npm install -g janitorr-pdk

# Create new plugin from template
janitorr-pdk create --type cleanup --name my-plugin

# Validate plugin
janitorr-pdk validate my-plugin.jar

# Test plugin locally
janitorr-pdk test my-plugin.jar --janitorr-url http://localhost:8080

# Package for distribution
janitorr-pdk package my-plugin/

# Publish to marketplace
janitorr-pdk publish my-plugin-1.0.0.jar --api-key YOUR_KEY
```

### Plugin Templates

Available templates:
- `cleanup-plugin` - Basic cleanup extension
- `notification-plugin` - Notification channel
- `data-source-plugin` - External data integration
- `ui-plugin` - UI widget/page extension
- `utility-plugin` - General utility

### Development Workflow

1. **Scaffold**: `janitorr-pdk create` generates project structure
2. **Develop**: Implement plugin interfaces
3. **Test**: Use local development server
4. **Debug**: Debug console with runtime logging
5. **Validate**: Run validation checks
6. **Package**: Create distributable JAR
7. **Publish**: Upload to marketplace

## Integration with Existing Architecture

### Spring Boot Integration

```kotlin
@Configuration
@ConditionalOnProperty(name = "janitorr.plugins.enabled", havingValue = "true")
class PluginConfiguration {
    
    @Bean
    fun pluginManager(
        applicationContext: ApplicationContext,
        pluginProperties: PluginProperties
    ): PluginManager {
        return PluginManagerImpl(applicationContext, pluginProperties)
    }
    
    @Bean
    fun pluginRegistry(): PluginRegistry {
        return PluginRegistryImpl()
    }
    
    @Bean
    fun pluginSecurityManager(): PluginSecurityManager {
        return PluginSecurityManagerImpl()
    }
}
```

### Configuration Properties

```yaml
janitorr:
  plugins:
    enabled: true
    directory: "/config/plugins"
    hot-reload: true
    marketplace:
      enabled: true
      url: "https://marketplace.janitorr.app"
      auto-update: false
    security:
      signature-verification: true
      trusted-publishers: []
```

### API Extensions

Plugins can extend existing REST API:

```kotlin
@RestController
@RequestMapping("/api/plugins")
@Profile("!leyden")
class PluginApiController(
    private val pluginManager: PluginManager
) {
    @GetMapping
    fun listPlugins(): List<PluginInfo>
    
    @PostMapping("/{id}/enable")
    fun enablePlugin(@PathVariable id: String): PluginStatus
    
    @PostMapping("/{id}/disable")
    fun disablePlugin(@PathVariable id: String): PluginStatus
    
    @DeleteMapping("/{id}")
    fun uninstallPlugin(@PathVariable id: String): Boolean
    
    @GetMapping("/marketplace")
    fun browseMarketplace(): List<MarketplacePlugin>
    
    @PostMapping("/install")
    fun installPlugin(@RequestBody request: InstallRequest): PluginStatus
}
```

## Implementation Roadmap

### Phase 1: Foundation (Months 1-2)
- Core plugin interfaces
- Plugin discovery mechanism
- Basic classloader isolation
- Plugin manifest parsing
- Configuration system

### Phase 2: Security (Month 3)
- Permission system
- Resource limits
- API whitelisting
- Code signing
- Validation framework

### Phase 3: SDK (Month 4)
- SDK implementation
- Helper utilities
- Plugin templates
- Development tools

### Phase 4: UI Integration (Month 5)
- Plugin manager UI
- Configuration panels
- Monitoring dashboard
- Status indicators

### Phase 5: Marketplace (Month 6)
- Marketplace backend
- Publishing workflow
- Rating/review system
- Auto-updates

### Phase 6: Polish & Documentation (Month 7)
- Comprehensive documentation
- Example plugins
- Migration guides
- Performance optimization

## Backward Compatibility

The plugin system is designed to be completely optional:

1. **Feature Flag**: Disabled by default, opt-in via configuration
2. **No Breaking Changes**: Existing functionality unchanged
3. **Graceful Degradation**: If plugins fail, core features continue
4. **Profile Support**: Can be excluded from native builds if needed

## Testing Strategy

### Unit Tests
- Plugin interface implementations
- Classloader isolation
- Permission enforcement
- Configuration parsing

### Integration Tests
- Plugin lifecycle management
- API access from plugins
- Resource limit enforcement
- Multi-plugin scenarios

### Security Tests
- Permission violations
- Resource exhaustion attempts
- API access violations
- Malicious code detection

### Performance Tests
- Plugin startup time
- Memory overhead
- CPU usage
- Hot reload performance

## Monitoring & Observability

### Plugin Metrics

```kotlin
data class PluginMetrics(
    val pluginId: String,
    val state: PluginState,
    val uptime: Duration,
    val cpuUsage: Double,
    val memoryUsage: Long,
    val apiCallCount: Long,
    val errorCount: Long,
    val lastError: String?
)
```

### Logging

Plugins have isolated loggers:
```kotlin
interface PluginLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}
```

Logs are written to: `/logs/plugins/{plugin-id}.log`

## Conclusion

This architecture provides a robust, secure, and extensible plugin system for Janitorr. The phased implementation approach allows for incremental development while maintaining backward compatibility and system stability.
