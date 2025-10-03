# Plugin Marketplace Specification

This document specifies the architecture, features, and implementation details for the Janitorr Plugin Marketplace.

## Overview

The Plugin Marketplace is a centralized platform for discovering, installing, and managing Janitorr plugins. It provides a secure, user-friendly way for the community to share and distribute plugins.

## System Architecture

### High-Level Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Marketplace Frontend                     │
│  (Integrated into Janitorr Management UI + Standalone Web)  │
└────────────────────┬────────────────────────────────────────┘
                     │ REST API
                     ↓
┌─────────────────────────────────────────────────────────────┐
│                   Marketplace Backend                       │
├─────────────────────────────────────────────────────────────┤
│  API Gateway (Spring Cloud Gateway)                         │
│  ├─── Authentication/Authorization                          │
│  ├─── Rate Limiting                                         │
│  └─── Request Routing                                       │
├─────────────────────────────────────────────────────────────┤
│  Microservices                                              │
│  ├─── Plugin Repository Service                            │
│  ├─── Version Management Service                           │
│  ├─── Download Service                                     │
│  ├─── Publishing Service                                   │
│  ├─── Validation Service                                   │
│  ├─── Statistics Service                                   │
│  ├─── Review & Rating Service                              │
│  └─── Moderation Service                                   │
├─────────────────────────────────────────────────────────────┤
│  Data Layer                                                 │
│  ├─── PostgreSQL (Metadata)                                │
│  ├─── S3/MinIO (Plugin JARs)                               │
│  └─── Redis (Caching)                                       │
└─────────────────────────────────────────────────────────────┘
```

## Core Features

### 1. Plugin Discovery & Browsing

#### Browse by Category

```
┌─────────────────────────────────────────────────────────┐
│  🔍 Search: [                              ] 🔎         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Categories:                                            │
│  [All] [Cleanup] [Notifications] [Data] [UI] [Utility] │
│                                                         │
│  Sort by: [Popularity ▼] Filter: [Compatible ▼]        │
│                                                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 🧹 Advanced Duplicate Finder          ⭐ 4.8/5  │  │
│  │ by CommunityDev                    📥 12.5K      │  │
│  │ AI-powered duplicate detection...              │  │
│  │ [Install]  [Details]                           │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 📢 Slack Integration                 ⭐ 4.9/5   │  │
│  │ by Official                        📥 8.2K       │  │
│  │ Send notifications to Slack...                  │  │
│  │ [Installed ✓]  [Configure]                     │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

#### Search Functionality

- Full-text search across plugin names, descriptions, and tags
- Fuzzy matching for typo tolerance
- Auto-suggestions as user types
- Search filters:
  - Category
  - Compatibility (API version)
  - Rating threshold
  - Download count
  - Last updated date

#### Featured & Trending

- **Editor's Picks**: Manually curated list of high-quality plugins
- **Trending**: Recently popular plugins based on download velocity
- **New Releases**: Recently published or updated plugins
- **Most Downloaded**: All-time most popular plugins

### 2. Plugin Details Page

```
┌─────────────────────────────────────────────────────────┐
│  🧹 Advanced Duplicate Finder                          │
│  by CommunityDev                           v1.2.0      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ⭐⭐⭐⭐⭐ 4.8/5 (127 reviews)                         │
│  📥 12,542 downloads                                   │
│  📅 Last updated: 2024-10-01                          │
│  ✅ Compatible with Janitorr 2.0+                     │
│                                                         │
│  [Install v1.2.0 ▼]  [View Source]  [Report Issue]    │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  Description                                            │
│  ─────────────                                          │
│  AI-powered duplicate detection with fuzzy matching.   │
│  Uses advanced algorithms to identify similar content  │
│  and helps you clean up duplicate files efficiently.   │
│                                                         │
│  Features                                               │
│  ────────                                               │
│  • Fuzzy title matching                                │
│  • Configurable similarity threshold                   │
│  • Batch processing support                            │
│  • Automatic quality preference                        │
│                                                         │
│  Screenshots                                            │
│  ───────────                                            │
│  [📸 Configuration] [📸 Results] [📸 Dashboard]        │
│                                                         │
│  Permissions Required                                   │
│  ────────────────────                                   │
│  • Read media library (api.media.read)                 │
│  • Delete media files (api.media.write)                │
│  • Send notifications (api.notification.send)          │
│                                                         │
│  Configuration Options                                  │
│  ────────────────────                                   │
│  • similarityThreshold: 0.0-1.0 (default: 0.85)       │
│  • preferHigherQuality: boolean (default: true)        │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  Reviews (127)                          [Write Review]  │
│  ──────────────────────────────────────────────────────│
│  ⭐⭐⭐⭐⭐ John D. - 2024-09-28                        │
│  "Excellent plugin! Saved me 500GB of duplicates."    │
│  👍 42  👎 2                                           │
│                                                         │
│  ⭐⭐⭐⭐☆ Sarah M. - 2024-09-25                       │
│  "Works well but needs better configuration docs."     │
│  👍 18  👎 1                                           │
└─────────────────────────────────────────────────────────┘
```

### 3. One-Click Installation

#### Installation Flow

```
User clicks "Install"
        │
        ↓
Check API compatibility
        │
        ├─── Incompatible ───> Show error message
        │
        ↓ Compatible
        │
Check dependencies
        │
        ├─── Missing deps ───> Prompt to install dependencies
        │
        ↓ All satisfied
        │
Show permission request
        │
        ├─── User denies ───> Cancel installation
        │
        ↓ User approves
        │
Download plugin JAR
        │
        ↓
Verify signature
        │
        ├─── Invalid ───> Show security warning
        │
        ↓ Valid
        │
Install to plugin directory
        │
        ↓
Load plugin (or schedule for restart)
        │
        ↓
Show success notification
        │
        ↓
Redirect to configuration page
```

#### API Endpoint

```http
POST /api/marketplace/install
Content-Type: application/json

{
  "pluginId": "advanced-duplicate-finder",
  "version": "1.2.0",
  "acceptPermissions": true,
  "installDependencies": true
}

Response:
{
  "success": true,
  "installationId": "inst-12345",
  "status": "installed",
  "message": "Plugin installed successfully",
  "requiresRestart": false
}
```

### 4. Auto-Updates

#### Update Check Service

Runs on configurable schedule (default: daily):

```kotlin
@Scheduled(cron = "\${janitorr.plugins.marketplace.update-check-cron}")
fun checkForUpdates() {
    val installedPlugins = pluginManager.getInstalledPlugins()
    
    installedPlugins.forEach { plugin ->
        val latestVersion = marketplaceClient.getLatestVersion(plugin.id)
        
        if (isNewer(latestVersion, plugin.version)) {
            notifyUpdateAvailable(plugin, latestVersion)
        }
    }
}
```

#### Update UI

```
┌─────────────────────────────────────────────────────────┐
│  🔔 Plugin Updates Available (3)                        │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Advanced Duplicate Finder                             │
│  1.2.0 → 1.3.0                                         │
│  • Added support for video quality comparison          │
│  • Fixed memory leak in batch processing               │
│  [Update] [Skip]                                       │
│                                                         │
│  Slack Integration                                      │
│  2.1.0 → 2.2.0  ⚠️ Breaking Changes                    │
│  • New authentication method (requires reconfiguration)│
│  [View Changes] [Update] [Skip]                        │
│                                                         │
│  [ ] Auto-update compatible plugins                    │
│  [Update All Compatible]                               │
└─────────────────────────────────────────────────────────┘
```

### 5. Rating & Review System

#### Rating Structure

```kotlin
data class PluginRating(
    val pluginId: String,
    val userId: String,
    val stars: Int, // 1-5
    val review: String?,
    val compatibility: CompatibilityReport,
    val createdAt: Long,
    val helpful: Int = 0,
    val notHelpful: Int = 0
)

data class CompatibilityReport(
    val janitorrVersion: String,
    val worked: Boolean,
    val issues: List<String> = emptyList()
)
```

#### Review Guidelines

- Reviews must be from verified plugin users
- Minimum 10-character review text (if provided)
- One review per user per plugin version
- Reviews can be updated as plugin is updated
- Moderation for spam/abuse

#### Helpful Vote System

- Users can mark reviews as helpful/not helpful
- Reviews sorted by helpfulness score
- Score = (helpful - notHelpful) / total votes

### 6. Dependency Resolution

#### Dependency Graph

```yaml
# Example: Plugin with dependencies
name: "advanced-analytics-plugin"
version: "2.0.0"
dependencies:
  janitorr-core:
    version: ">=2.0.0"
  chart-library:
    version: "^1.5.0"
    optional: false
  data-export-utils:
    version: "~0.3.0"
    optional: true
```

#### Resolution Algorithm

```kotlin
fun resolveDependencies(plugin: PluginManifest): DependencyTree {
    val tree = DependencyTree(plugin)
    val queue = Queue<Dependency>(plugin.dependencies)
    val resolved = mutableSetOf<String>()
    
    while (queue.isNotEmpty()) {
        val dep = queue.dequeue()
        
        if (dep.name in resolved) continue
        
        val depPlugin = marketplaceClient.findPlugin(dep.name, dep.version)
        
        if (depPlugin == null) {
            if (dep.optional) {
                tree.addMissing(dep, optional = true)
                continue
            } else {
                throw DependencyNotFoundException(dep)
            }
        }
        
        tree.add(depPlugin)
        resolved.add(dep.name)
        
        // Add transitive dependencies
        depPlugin.dependencies.forEach { transitiveDep ->
            if (transitiveDep.name !in resolved) {
                queue.enqueue(transitiveDep)
            }
        }
    }
    
    return tree
}
```

### 7. Version Management

#### Semantic Versioning

All plugins must follow semantic versioning (semver):
- **Major**: Breaking changes
- **Minor**: New features, backward compatible
- **Patch**: Bug fixes, backward compatible

#### Version Selectors

Users can pin versions or use ranges:

```yaml
dependencies:
  some-plugin:
    version: "1.2.3"      # Exact version
    version: "^1.2.0"     # Compatible with 1.2.0 (>= 1.2.0, < 2.0.0)
    version: "~1.2.0"     # Approximately 1.2.0 (>= 1.2.0, < 1.3.0)
    version: ">=1.0.0"    # Greater than or equal to 1.0.0
    version: "1.x"        # Any 1.x version
```

#### Rollback Support

```
┌─────────────────────────────────────────────────────────┐
│  Plugin Version History                                 │
├─────────────────────────────────────────────────────────┤
│  Advanced Duplicate Finder                             │
│                                                         │
│  ✓ v1.3.0 (current) - 2024-10-01                       │
│    Added video quality comparison                      │
│    [Configure]                                         │
│                                                         │
│    v1.2.0 - 2024-09-15                                │
│    Fixed memory leak                                   │
│    [Rollback to this version]                          │
│                                                         │
│    v1.1.0 - 2024-09-01                                │
│    Initial release                                     │
│    [Rollback to this version]                          │
└─────────────────────────────────────────────────────────┘
```

## Publishing Workflow

### Developer Registration

1. Create account with email verification
2. Accept developer terms of service
3. Optional: Link GitHub account for source verification
4. Generate API key for CLI publishing

### Plugin Submission

```bash
# Using janitorr-pdk CLI
janitorr-pdk publish my-plugin-1.0.0.jar \
  --api-key YOUR_API_KEY \
  --description "Plugin description" \
  --tags "cleanup,automation" \
  --category "cleanup" \
  --changelog "CHANGELOG.md" \
  --screenshots "screenshot1.png,screenshot2.png"
```

### Submission Process

```
Developer submits plugin
        │
        ↓
Automated validation
├─── JAR structure check
├─── Manifest validation
├─── API compatibility check
├─── Security scan (OWASP dependency check)
├─── Code signing verification (if required)
└─── Malware scan
        │
        ├─── Failed ───> Notify developer, reject submission
        │
        ↓ Passed
        │
Upload to staging
        │
        ↓
Beta testing period (optional, 7 days)
├─── Community testers can install from staging
└─── Collect compatibility reports
        │
        ↓
Moderation review (for first-time publishers)
├─── Manual code review
├─── Permission audit
└─── Quality assessment
        │
        ├─── Rejected ───> Notify developer with feedback
        │
        ↓ Approved
        │
Publish to marketplace
        │
        ↓
Notify subscribers
        │
        ↓
Available for installation
```

### Automated Validation Rules

1. **JAR Structure**
   - Must contain `plugin.yml` at root
   - Manifest must specify `Plugin-Class`
   - All referenced classes must exist

2. **API Compatibility**
   - Declared API version must be supported
   - Plugin must implement valid plugin interface
   - Required methods must be present

3. **Security**
   - No known vulnerable dependencies
   - No obfuscated code (for public plugins)
   - Requested permissions must be reasonable
   - Code signing (optional but recommended)

4. **Resource Limits**
   - Declared resource limits must be reasonable
   - Maximum JAR size: 50MB
   - Maximum dependency count: 20

5. **Quality**
   - Plugin must have valid metadata
   - Description minimum length: 50 characters
   - At least one screenshot (recommended)

### Moderation Guidelines

**Automatic Approval:**
- Updates from trusted publishers
- Plugins from verified organizations
- Patches (x.x.N updates) from known publishers

**Manual Review Required:**
- First-time publishers
- Major version updates (X.0.0)
- Plugins requesting elevated permissions
- Community-reported issues

**Rejection Reasons:**
- Malicious code detected
- Trademark/copyright violation
- Duplicate of existing plugin
- Poor code quality
- Insufficient documentation
- Unreasonable resource requests

## API Specification

### REST API Endpoints

#### Browse Plugins

```http
GET /api/marketplace/plugins
Query parameters:
  - category: string (optional)
  - search: string (optional)
  - sort: popularity|rating|downloads|updated (default: popularity)
  - page: number (default: 1)
  - limit: number (default: 20, max: 100)
  - compatibleWith: string (Janitorr version, optional)

Response:
{
  "plugins": [
    {
      "id": "advanced-duplicate-finder",
      "name": "Advanced Duplicate Finder",
      "version": "1.2.0",
      "author": "CommunityDev",
      "description": "AI-powered duplicate detection...",
      "category": "cleanup",
      "rating": 4.8,
      "downloadCount": 12542,
      "lastUpdated": "2024-10-01T00:00:00Z",
      "compatible": true
    }
  ],
  "totalCount": 127,
  "page": 1,
  "pageSize": 20
}
```

#### Get Plugin Details

```http
GET /api/marketplace/plugins/{pluginId}

Response:
{
  "id": "advanced-duplicate-finder",
  "name": "Advanced Duplicate Finder",
  "currentVersion": "1.2.0",
  "versions": ["1.2.0", "1.1.0", "1.0.0"],
  "author": {
    "name": "CommunityDev",
    "verified": true
  },
  "description": "Full description...",
  "longDescription": "Extended markdown description...",
  "category": "cleanup",
  "tags": ["cleanup", "duplicates", "ai"],
  "rating": {
    "average": 4.8,
    "count": 127
  },
  "downloads": {
    "total": 12542,
    "lastMonth": 842
  },
  "screenshots": [
    "https://cdn.janitorr.app/screenshots/plugin1-1.png"
  ],
  "changelog": "...",
  "permissions": [
    "api.media.read",
    "api.media.write"
  ],
  "compatibility": {
    "minJanitorrVersion": "2.0.0",
    "maxJanitorrVersion": null
  },
  "dependencies": [...],
  "sourceUrl": "https://github.com/user/plugin",
  "issuesUrl": "https://github.com/user/plugin/issues",
  "licenseUrl": "https://github.com/user/plugin/blob/main/LICENSE"
}
```

#### Install Plugin

```http
POST /api/marketplace/install
Content-Type: application/json
Authorization: Bearer {user-token}

{
  "pluginId": "advanced-duplicate-finder",
  "version": "1.2.0",
  "acceptPermissions": true,
  "installDependencies": true
}

Response:
{
  "success": true,
  "installationId": "inst-12345",
  "status": "installed",
  "message": "Plugin installed successfully",
  "requiresRestart": false,
  "dependencies": [
    {
      "name": "chart-library",
      "version": "1.5.2",
      "installed": true
    }
  ]
}
```

#### Submit Plugin

```http
POST /api/marketplace/publish
Content-Type: multipart/form-data
Authorization: Bearer {api-key}

Form data:
  - file: plugin.jar (required)
  - changelog: string (optional)
  - screenshots[]: files (optional, max 5)

Response:
{
  "success": true,
  "pluginId": "advanced-duplicate-finder",
  "version": "1.3.0",
  "status": "under-review",
  "estimatedReviewTime": "2-4 hours",
  "submissionId": "sub-67890"
}
```

## Statistics & Analytics

### Publisher Dashboard

```
┌─────────────────────────────────────────────────────────┐
│  📊 Plugin Statistics Dashboard                        │
├─────────────────────────────────────────────────────────┤
│  Advanced Duplicate Finder v1.2.0                      │
│                                                         │
│  Total Downloads: 12,542    This Month: 842            │
│  Active Installs: 3,247     Avg Rating: 4.8/5         │
│                                                         │
│  Downloads Trend (Last 30 Days)                        │
│  ┌─────────────────────────────────────────────────┐  │
│  │     ╭─╮                                          │  │
│  │   ╭─╯ ╰─╮  ╭─╮                                  │  │
│  │ ╭─╯      ╰─╯ ╰─╮                                │  │
│  │─╯              ╰──────────                      │  │
│  └─────────────────────────────────────────────────┘  │
│                                                         │
│  Version Distribution                                   │
│  v1.2.0: ████████████████████ 65%                     │
│  v1.1.0: ████████ 25%                                 │
│  v1.0.0: ████ 10%                                     │
│                                                         │
│  Compatibility Reports                                  │
│  ✅ Working: 98.5%                                     │
│  ⚠️  Issues: 1.2%                                      │
│  ❌ Errors: 0.3%                                       │
└─────────────────────────────────────────────────────────┘
```

### Tracked Metrics

- Downloads (total, by version, by time period)
- Active installations
- Ratings and reviews
- Compatibility reports
- Error rates
- Resource usage statistics
- Update adoption rate

## Security Considerations

### Code Signing

Optional but recommended for trusted publishers:

```bash
# Sign plugin JAR
jarsigner -keystore developer.keystore \
  -signedjar my-plugin-signed.jar \
  my-plugin.jar \
  developer-alias
```

Marketplace verifies signature on upload.

### Vulnerability Scanning

All plugins scanned for:
- Known vulnerable dependencies (OWASP Dependency Check)
- Malware patterns
- Suspicious permissions
- Code obfuscation

### Trust Levels

1. **Verified Publisher**: Organization verified, high trust
2. **Trusted Publisher**: Good track record, medium-high trust
3. **Community Publisher**: No issues reported, medium trust
4. **New Publisher**: First submission, requires review
5. **Flagged Publisher**: Community reports, increased scrutiny

### Privacy

- User installation data is anonymous
- Statistics aggregated, not individual
- Reviews can be anonymous or attributed
- Plugin code is public (for open-source plugins)

## Future Enhancements

- **Plugin Sponsorship**: Support plugin developers financially
- **Beta Testing Program**: Early access to plugin updates
- **Plugin Collections**: Curated sets of related plugins
- **A/B Testing**: Try different plugin versions
- **Advanced Analytics**: Detailed usage analytics for publishers
- **Plugin Recommendations**: AI-based plugin suggestions
- **Integration Testing**: Automated compatibility testing
- **Multi-language Support**: Plugins in multiple languages

## Implementation Timeline

### Phase 1 (Month 1-2): Core Infrastructure
- Basic REST API
- Plugin repository
- Download service
- Simple web interface

### Phase 2 (Month 3): Publishing
- Developer registration
- Plugin submission
- Automated validation
- Version management

### Phase 3 (Month 4): Discovery
- Search functionality
- Category browsing
- Featured plugins
- Install API

### Phase 4 (Month 5): Social Features
- Rating system
- Review system
- Publisher dashboard
- Statistics

### Phase 5 (Month 6): Advanced Features
- Auto-updates
- Dependency resolution
- Beta testing
- Moderation tools

### Phase 6 (Month 7): Polish
- Performance optimization
- Enhanced security
- Mobile-friendly UI
- Comprehensive documentation
