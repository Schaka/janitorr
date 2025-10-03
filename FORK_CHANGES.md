# Fork Changes: carcheky/janitorr

This document tracks the changes and additions made in this fork compared to the upstream [schaka/janitorr](https://github.com/schaka/janitorr) repository.

## Fork-Specific Features

### 1. Comprehensive Multi-Language Documentation

Added extensive documentation in both English and Spanish:

- **English Documentation** (`docs/wiki/en/`)
  - [Home](docs/wiki/en/Home.md) - Wiki homepage with overview and navigation
  - [Docker Compose Setup](docs/wiki/en/Docker-Compose-Setup.md) - Complete deployment guide
  - [Configuration Guide](docs/wiki/en/Configuration-Guide.md) - Detailed configuration reference
  - [FAQ](docs/wiki/en/FAQ.md) - Frequently Asked Questions
  - [Troubleshooting](docs/wiki/en/Troubleshooting.md) - Common issues and solutions

- **Spanish Documentation** (`docs/wiki/es/`)
  - [Inicio](docs/wiki/es/Home.md) - Página de inicio
  - [Configuración Docker Compose](docs/wiki/es/Configuracion-Docker-Compose.md) - Guía de implementación
  - [Guía de Configuración](docs/wiki/es/Guia-Configuracion.md) - Referencia de configuración
  - [Preguntas Frecuentes](docs/wiki/es/Preguntas-Frecuentes.md) - Preguntas comunes
  - [Solución de Problemas](docs/wiki/es/Solucion-Problemas.md) - Problemas comunes y soluciones

### 2. Management Web UI

Added a web-based management interface for easier configuration and monitoring:

- **Frontend** (`src/main/resources/static/`)
  - `index.html` - Main UI page
  - `app.js` - JavaScript application logic
  - `styles.css` - UI styling

- **Backend** (`src/main/kotlin/com/github/schaka/janitorr/api/`)
  - `ManagementController.kt` - REST API controller for the management UI

See [MANAGEMENT_UI.md](MANAGEMENT_UI.md) for detailed documentation.

### 3. Build Configuration Enhancements

- **Dynamic Repository Owner Support**: Modified `build.gradle.kts` to dynamically determine the repository owner from the `GITHUB_REPOSITORY` environment variable, allowing forks to build images with their own registry names.
  
  ```kotlin
  val repoOwner = System.getenv("GITHUB_REPOSITORY")?.split("/")?.get(0) ?: "schaka"
  val containerImageName = "ghcr.io/$repoOwner/${project.name}"
  ```

### 4. Safer Default Configuration

Changed default values to be more conservative and safer for new users:

- **Media Deletion**: `enabled: false` (upstream default: `true`)
- **Tag-Based Deletion**: `enabled: false` (upstream default: `true`)
- **Episode Deletion**: `enabled: false` (upstream default: `true`)

This prevents accidental media deletion for users who are just getting started with Janitorr.

### 5. Documentation Metadata Files

Added comprehensive documentation about the wiki and management UI:

- `WIKI_DOCUMENTATION.md` - Overview of wiki documentation structure
- `docs/wiki/README.md` - Detailed wiki documentation guide

## Workflow Enhancements

- Added `main` branch to CI/CD workflow triggers to support fork's branching strategy

## Upstream Tracking

### Last Synced Commit
- **Upstream Repository**: https://github.com/schaka/janitorr
- **Last Sync**: 2025-10-03
- **Fork Base**: schaka/janitorr@bffb15e
- **Upstream HEAD**: schaka/janitorr@7c6d9c2

### Known Compatibility Status

The fork and upstream are functionally equivalent in terms of core features. The main differences are:

1. **Fork Additions**: Management UI and comprehensive documentation (see above)
2. **Safer Defaults**: Fork uses `enabled: false` for deletion features by default
3. **Build Configuration**: Fork supports dynamic repository owner in build system

Both fork and upstream:
- Require Java 24+ (as of latest upstream)
- Use Kotlin 2.2.20
- Support the same core features (Jellyfin/Emby, Sonarr/Radarr, Jellyseerr integration)
- Support Jellystat and Streamystats for viewing statistics

### Upstream Features Available (Not Yet Merged)

The following features exist in newer upstream commits but are not yet integrated into this fork:

1. **Java 25 with Leyden Support** (#181) - commit 9768ec6
   - Performance improvements using Java 25 and Project Leyden
   - Note: Current fork and upstream both require Java 24+

2. **Build Optimizations** - commits 7c6d9c2, ea4f564
   - Experimental header feature to lower memory footprint
   - Documentation adjustments

3. **File Access Improvements** - commit b6bb396
   - Clean up requirements for file access

4. **Multiple Exclusion Tags** - commit 5f526ab
   - Support for multiple exclusion tags in Sonarr/Radarr for more granular control

5. **ImportList Exclusions** - commit 1817243
   - Ability to exclude items from import lists in *arr applications

6. **Leaving-Soon Folder Structure Improvements** - commit 825b573 (#164)
   - Enhanced folder structure for "leaving soon" collections to support extended folder structure for movies

7. **Streamystats 2.4.0 Support** - commits ed7c0d5, b3407dd
   - Integration with Streamystats 2.4.0 for viewing statistics

These features are available in upstream but have not been merged to avoid disrupting the fork's stability and to maintain the custom additions (Management UI and documentation). Users who need these features can consider using the upstream repository directly or contributing a merge request that preserves the fork-specific features.

## Merging Strategy

When syncing from upstream:

1. **Preserve Fork-Specific Features**: All documentation, Management UI, and build enhancements must be preserved
2. **Cherry-Pick Bug Fixes**: Important bug fixes should be cherry-picked from upstream
3. **Selective Feature Integration**: New features from upstream should be evaluated and integrated selectively
4. **Maintain Safer Defaults**: Keep the more conservative default configuration values

## Contributing

When contributing to this fork:

1. **Document Changes**: Update this file when adding new fork-specific features
2. **Maintain Translations**: Keep both English and Spanish documentation in sync
3. **Test Management UI**: Ensure any configuration changes are reflected in the management interface
4. **Preserve Compatibility**: Ensure changes don't break the safer defaults philosophy

## License

This fork maintains the same license as the upstream project. See [LICENSE.txt](LICENSE.txt) for details.

---

**Note**: This is a community-maintained fork with additional features. For the original project, see [schaka/janitorr](https://github.com/schaka/janitorr).
