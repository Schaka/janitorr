# Fork Maintenance Guide

Quick reference for maintaining the carcheky/janitorr fork.

## Fork Overview

This is a feature-enhanced fork of [schaka/janitorr](https://github.com/schaka/janitorr) with:
- Management Web UI
- Comprehensive English/Spanish documentation  
- Safer default configuration
- Build system enhancements

## Key Files to Preserve

When syncing with upstream, NEVER overwrite these files:

```
docs/wiki/                          # All documentation
src/main/resources/static/          # Web UI files
src/main/kotlin/.../ManagementController.kt
FORK_CHANGES.md
UPSTREAM_SYNC_STATUS.md
WIKI_DOCUMENTATION.md
MANAGEMENT_UI.md
```

## Configuration Defaults

Fork uses safer defaults:
```yaml
media-deletion:
  enabled: false    # upstream: true
tag-based-deletion:
  enabled: false    # upstream: true
episode-deletion:
  enabled: false    # upstream: true
```

## Syncing with Upstream

### Setup (one time)

```bash
git remote add upstream https://github.com/schaka/janitorr.git
git fetch upstream
```

### Check for Updates

```bash
git fetch upstream
git log HEAD..upstream/main --oneline
```

### Cherry-Pick Specific Commits

```bash
# Pick a specific bug fix
git cherry-pick <commit-hash>

# If conflicts occur:
git status
# Edit conflicted files, keep fork-specific code
git add <files>
git cherry-pick --continue
```

### Never Do This

❌ `git merge upstream/main` - Will try to delete fork features
❌ `git rebase upstream/main` - Will rewrite history and lose fork commits

## Build Configuration

### Dynamic Repository Owner

The fork supports building with any repository owner:

```kotlin
val repoOwner = System.getenv("GITHUB_REPOSITORY")?.split("/")?.get(0) ?: "schaka"
val containerImageName = "ghcr.io/$repoOwner/${project.name}"
```

This allows forks to build images with their own registry name automatically.

### Building

```bash
./gradlew clean build
```

Requires Java 24+

## Documentation

### Structure

```
docs/wiki/
├── README.md                    # Documentation overview
├── en/                          # English docs
│   ├── Home.md
│   ├── Docker-Compose-Setup.md
│   ├── Configuration-Guide.md
│   ├── FAQ.md
│   └── Troubleshooting.md
└── es/                          # Spanish docs
    ├── Home.md
    ├── Configuracion-Docker-Compose.md
    ├── Guia-Configuracion.md
    ├── Preguntas-Frecuentes.md
    └── Solucion-Problemas.md
```

### Updating Documentation

When adding features:
1. Update both `en/` and `es/` versions
2. Update `docs/wiki/README.md`
3. Update cross-references in other docs

## Management UI

### Location

- Frontend: `src/main/resources/static/`
- Backend: `src/main/kotlin/com/github/schaka/janitorr/api/ManagementController.kt`

### Testing

```bash
./gradlew bootRun
# Access http://localhost:8978/
```

## Contributing

### For Fork-Specific Features

1. Make changes to fork-specific code
2. Update documentation (both languages)
3. Test Management UI if configuration changes
4. Update FORK_CHANGES.md if adding major features

### For Upstream Integration

1. Check if feature exists in upstream
2. If yes, cherry-pick from upstream
3. If no, implement and consider contributing to upstream
4. Test compatibility with fork features

## Version Management

### Release Process

1. Update `gradle.properties` version
2. Update `FORK_CHANGES.md` with version notes
3. Tag release: `git tag -a v1.x.x -m "Release v1.x.x"`
4. Push tag: `git push origin v1.x.x`

### Tracking Upstream

Track upstream version in `UPSTREAM_SYNC_STATUS.md`:
```markdown
Last Sync: YYYY-MM-DD
Upstream Commit: <hash>
```

## Common Tasks

### Add New Configuration Option

1. Update `application-template.yml`
2. Update configuration classes if needed
3. Update docs: `en/Configuration-Guide.md` and `es/Guia-Configuracion.md`
4. Update Management UI if applicable

### Fix a Bug

1. Check if bug exists in upstream
2. If yes, cherry-pick the fix
3. If no, fix locally and consider contributing to upstream

### Update Dependencies

1. Check upstream's `build.gradle.kts` for updates
2. Test compatibility with fork features
3. Update and test build

## Troubleshooting

### Build Fails

- Ensure Java 24+ is installed
- Clear Gradle cache: `./gradlew clean --no-daemon`
- Check `build.gradle.kts` for syntax errors

### Merge Conflicts

- Always keep fork-specific files (see "Key Files to Preserve")
- For shared files, manually merge keeping fork enhancements
- Test after resolving

### Documentation Out of Sync

- Update both English and Spanish versions
- Check all cross-references
- Verify links work

## Resources

- **Fork Documentation**: [FORK_CHANGES.md](FORK_CHANGES.md)
- **Sync Status**: [UPSTREAM_SYNC_STATUS.md](UPSTREAM_SYNC_STATUS.md)
- **Upstream Repo**: https://github.com/schaka/janitorr
- **Web UI Docs**: [MANAGEMENT_UI.md](MANAGEMENT_UI.md)

## Quick Commands

```bash
# Check status vs upstream
git fetch upstream
git log HEAD..upstream/main --oneline

# View specific upstream commit
git show upstream/main:<file>

# Compare files
git diff HEAD upstream/main -- <file>

# Build and run
./gradlew clean build bootRun

# Run tests
./gradlew test

# Build Docker image
./gradlew bootBuildImage
```

---

**Tip**: Bookmark this file for quick reference when working with the fork!
