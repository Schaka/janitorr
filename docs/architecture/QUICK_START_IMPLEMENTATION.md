# Plugin System - Quick Start for Implementation

This quick start guide helps developers begin implementing the plugin system when the project is ready to move from design to development.

## Overview

The plugin system is currently in **design phase**. This guide outlines how to begin implementation when prioritized.

## Prerequisites

Before starting implementation:

1. ✅ All architecture documents reviewed
2. ✅ Community feedback incorporated
3. ✅ Technical decisions approved
4. ✅ Resources allocated
5. ✅ Development environment set up

## Implementation Phases

### Phase 1: Core Infrastructure (2 months)

**Goal**: Basic plugin loading and lifecycle management

#### Week 1-2: Project Structure

Create new packages:
```
src/main/kotlin/com/github/schaka/janitorr/plugin/
├── api/                    # Plugin interfaces
│   ├── Plugin.kt
│   ├── CleanupPlugin.kt
│   ├── NotificationPlugin.kt
│   ├── DataSourcePlugin.kt
│   └── UIPlugin.kt
├── core/                   # Core infrastructure
│   ├── PluginManager.kt
│   ├── PluginRegistry.kt
│   ├── PluginLoader.kt
│   └── PluginLifecycleManager.kt
├── config/                 # Configuration
│   ├── PluginProperties.kt
│   └── PluginConfiguration.kt
├── security/              # Security layer
│   ├── PluginSecurityManager.kt
│   └── PluginPermissionManager.kt
└── sdk/                   # SDK implementation
    ├── JanitorrPluginSDK.kt
    └── services/
        ├── MediaServiceImpl.kt
        ├── ConfigServiceImpl.kt
        └── NotificationServiceImpl.kt
```

#### Week 3-4: Basic Interfaces

Implement base interfaces:

```kotlin
// src/main/kotlin/com/github/schaka/janitorr/plugin/api/Plugin.kt
package com.github.schaka.janitorr.plugin.api

import com.github.schaka.janitorr.plugin.sdk.JanitorrPluginSDK

interface Plugin {
    fun onInit(sdk: JanitorrPluginSDK)
    fun onDestroy()
    fun getMetadata(): PluginMetadata
}

data class PluginMetadata(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val apiVersion: String
)
```

#### Week 5-6: Plugin Discovery

Implement plugin discovery:

```kotlin
// src/main/kotlin/com/github/schaka/janitorr/plugin/core/PluginDiscovery.kt
package com.github.schaka.janitorr.plugin.core

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

@Service
class PluginDiscovery(
    private val pluginProperties: PluginProperties
) {
    
    fun discoverPlugins(): List<PluginDescriptor> {
        val pluginDir = Path(pluginProperties.directory)
        
        if (!pluginDir.exists()) {
            Files.createDirectories(pluginDir)
            return emptyList()
        }
        
        return pluginDir
            .listDirectoryEntries("*.jar")
            .mapNotNull { loadPluginDescriptor(it) }
    }
    
    private fun loadPluginDescriptor(jarPath: Path): PluginDescriptor? {
        // Load plugin.yml from JAR
        // Parse manifest
        // Create PluginDescriptor
        // Return descriptor
        return null // TODO: Implement
    }
}
```

#### Week 7-8: Plugin Loading

Implement plugin loading with classloader isolation:

```kotlin
// src/main/kotlin/com/github/schaka/janitorr/plugin/core/PluginLoader.kt
package com.github.schaka.janitorr.plugin.core

import com.github.schaka.janitorr.plugin.api.Plugin
import org.springframework.stereotype.Service
import java.net.URLClassLoader
import java.nio.file.Path

@Service
class PluginLoader {
    
    private val pluginClassLoaders = mutableMapOf<String, URLClassLoader>()
    
    fun loadPlugin(descriptor: PluginDescriptor): Plugin? {
        try {
            val classLoader = createIsolatedClassLoader(descriptor.jarPath)
            pluginClassLoaders[descriptor.id] = classLoader
            
            val pluginClass = classLoader.loadClass(descriptor.mainClass)
            val plugin = pluginClass.getDeclaredConstructor().newInstance() as Plugin
            
            return plugin
        } catch (e: Exception) {
            // Log error
            return null
        }
    }
    
    private fun createIsolatedClassLoader(jarPath: Path): URLClassLoader {
        return URLClassLoader(
            arrayOf(jarPath.toUri().toURL()),
            this::class.java.classLoader
        )
    }
    
    fun unloadPlugin(pluginId: String) {
        pluginClassLoaders[pluginId]?.close()
        pluginClassLoaders.remove(pluginId)
    }
}
```

### Phase 2: Configuration (Month 3)

#### Spring Boot Configuration

```kotlin
// src/main/kotlin/com/github/schaka/janitorr/plugin/config/PluginProperties.kt
package com.github.schaka.janitorr.plugin.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "janitorr.plugins")
data class PluginProperties(
    var enabled: Boolean = false,
    var directory: String = "/config/plugins",
    var hotReload: Boolean = false,
    var marketplace: MarketplaceProperties = MarketplaceProperties()
)

data class MarketplaceProperties(
    var enabled: Boolean = false,
    var url: String = "https://marketplace.janitorr.app",
    var autoUpdate: Boolean = false
)
```

#### application.yml additions

```yaml
janitorr:
  plugins:
    enabled: false  # Disabled by default
    directory: "/config/plugins"
    hot-reload: false
    marketplace:
      enabled: false
      url: "https://marketplace.janitorr.app"
      auto-update: false
```

### Phase 3: SDK Implementation (Month 4)

#### Basic SDK

```kotlin
// src/main/kotlin/com/github/schaka/janitorr/plugin/sdk/JanitorrPluginSDKImpl.kt
package com.github.schaka.janitorr.plugin.sdk

import org.springframework.stereotype.Component

@Component
class JanitorrPluginSDKImpl(
    private val mediaServiceImpl: MediaServiceImpl,
    private val configServiceImpl: ConfigServiceImpl,
    private val notificationServiceImpl: NotificationServiceImpl,
    private val loggerFactory: PluginLoggerFactory,
    private val storageFactory: PluginStorageFactory,
    private val schedulerFactory: PluginSchedulerFactory
) {
    
    fun createSDK(pluginId: String): JanitorrPluginSDK {
        return object : JanitorrPluginSDK {
            override val version = "2.0.0"
            override val mediaService = mediaServiceImpl
            override val configService = configServiceImpl
            override val notificationService = notificationServiceImpl
            override val logger = loggerFactory.createLogger(pluginId)
            override val storage = storageFactory.createStorage(pluginId)
            override val scheduler = schedulerFactory.createScheduler(pluginId)
        }
    }
}
```

### Phase 4: Testing (Ongoing)

#### Unit Tests

```kotlin
// src/test/kotlin/com/github/schaka/janitorr/plugin/core/PluginLoaderTest.kt
package com.github.schaka.janitorr.plugin.core

import io.mockk.*
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PluginLoaderTest {
    
    private val pluginLoader = PluginLoader()
    
    @Test
    fun `should load valid plugin`() {
        // Create test plugin JAR
        // Load plugin
        // Assert loaded successfully
    }
    
    @Test
    fun `should handle invalid plugin gracefully`() {
        // Create invalid plugin JAR
        // Attempt to load
        // Assert returns null
    }
}
```

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/plugin-system-phase1
```

### 2. Implement in Small Steps

**Day 1-3**: Basic interfaces  
**Day 4-7**: Plugin discovery  
**Day 8-12**: Plugin loading  
**Day 13-15**: Testing  

### 3. Test Thoroughly

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Check coverage
./gradlew jacocoTestReport
```

### 4. Document As You Go

Update documentation with implementation details:
- API changes
- Configuration options
- Usage examples
- Migration guides

### 5. Create Pull Request

```bash
git add .
git commit -m "feat: implement plugin discovery and loading"
git push origin feature/plugin-system-phase1
```

Create PR with:
- Description of changes
- Testing performed
- Documentation updates
- Breaking changes (if any)

## Proof of Concept Plugin

Create a simple test plugin to validate the infrastructure:

```kotlin
// TestPlugin.kt
package com.example.testplugin

import com.github.schaka.janitorr.plugin.api.*

class TestPlugin : CleanupPlugin {
    
    private lateinit var sdk: JanitorrPluginSDK
    
    override fun onInit(sdk: JanitorrPluginSDK) {
        this.sdk = sdk
        sdk.logger.info("TestPlugin initialized!")
    }
    
    override fun onDestroy() {
        sdk.logger.info("TestPlugin destroyed")
    }
    
    override fun getMetadata() = PluginMetadata(
        name = "Test Plugin",
        version = "0.1.0",
        author = "Janitorr Team",
        description = "Simple test plugin",
        apiVersion = "2.0"
    )
    
    override fun shouldDelete(media: MediaItem, context: CleanupContext): Boolean {
        sdk.logger.debug("Evaluating: ${media.title}")
        return false // Don't delete anything
    }
    
    override fun executeCleanup(media: MediaItem): CleanupResult {
        return CleanupResult(
            success = true,
            message = "Test cleanup completed"
        )
    }
}
```

Plugin manifest (plugin.yml):
```yaml
name: "test-plugin"
version: "0.1.0"
apiVersion: "2.0"
main: "com.example.testplugin.TestPlugin"
author: "Janitorr Team"
description: "Simple test plugin for validation"
types:
  - "cleanup"
permissions: []
```

## Validation Checklist

Before moving to next phase:

- [ ] All interfaces compile successfully
- [ ] Plugin discovery finds test plugin
- [ ] Plugin loads without errors
- [ ] Plugin initialization executes
- [ ] Plugin methods callable
- [ ] Plugin unloads cleanly
- [ ] No memory leaks detected
- [ ] Unit tests pass (>80% coverage)
- [ ] Integration tests pass
- [ ] Documentation updated

## Common Issues and Solutions

### Issue: ClassLoader conflicts

**Solution**: Ensure each plugin has isolated classloader with proper parent delegation.

### Issue: Plugin fails to load

**Solution**: Check plugin.yml validity, verify main class exists, ensure API version compatible.

### Issue: Memory leaks on plugin unload

**Solution**: Ensure all resources cleaned up in `onDestroy()`, close classloaders properly.

### Issue: Spring context conflicts

**Solution**: Plugins should not create their own Spring context, use provided SDK instead.

## Resources

- [Plugin System Architecture](PLUGIN_SYSTEM_ARCHITECTURE.md)
- [Plugin Interfaces](PLUGIN_INTERFACES.md)
- [Development Guide](PLUGIN_DEVELOPMENT_GUIDE.md)
- [Marketplace Spec](PLUGIN_MARKETPLACE_SPEC.md)
- [Implementation Checklist](IMPLEMENTATION_CHECKLIST.md)

## Support

For implementation questions:
- GitHub Discussions: Architecture category
- Development channel: #plugin-system
- Documentation: https://docs.janitorr.app/plugins

## Next Steps

1. Review all architecture documents
2. Set up development environment
3. Create feature branch
4. Implement Phase 1 infrastructure
5. Build proof-of-concept plugin
6. Validate and test
7. Iterate based on feedback

---

**Status**: Ready for Implementation  
**Priority**: To be determined  
**Estimated Effort**: 7 months (phased approach)
