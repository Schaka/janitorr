# Upstream Sync Status

This document tracks the synchronization status between this fork (carcheky/janitorr) and the upstream repository (schaka/janitorr).

## Last Sync Details

- **Date**: October 3, 2025
- **Upstream Commit**: `7c6d9c2` - [Build] Try new experimental header feature to lower memory footprint
- **Upstream Branch**: `main`
- **Fork Commit Before Sync**: `bffb15e`

## Sync Analysis

### What Was NOT Merged (Intentionally Preserved)

The following fork-specific features were intentionally preserved and NOT replaced by upstream versions:

1. **Comprehensive Multi-Language Documentation** (`docs/wiki/`)
   - English and Spanish documentation (11 files, 3600+ lines)
   - Setup guides, configuration reference, FAQ, troubleshooting

2. **Management Web UI** 
   - Web-based interface for configuration and monitoring
   - Files: `src/main/resources/static/*`, `ManagementController.kt`

3. **Documentation Metadata Files**
   - `WIKI_DOCUMENTATION.md`
   - `MANAGEMENT_UI.md`
   - `docs/wiki/README.md`

4. **Build Enhancements**
   - Dynamic repository owner support (allows forks to use their own registry)

5. **Safer Default Configuration**
   - `enabled: false` for media deletion, tag deletion, and episode deletion
   - Upstream uses `enabled: true` which could lead to accidental deletions

### Upstream Changes NOT Applicable

The following upstream differences were analyzed and determined to NOT provide value for the fork:

1. **Workflow Trigger Changes**
   - Upstream removed `main` branch from CI triggers
   - Fork keeps `main` branch trigger to support fork's branching strategy

2. **Default Configuration Values**
   - Upstream changed defaults to `enabled: true` for deletion features
   - Fork intentionally uses `enabled: false` for safety

3. **Build Registry**
   - Upstream hardcodes `ghcr.io/schaka/janitorr`
   - Fork uses dynamic `GITHUB_REPOSITORY` environment variable

## Upstream Features Available But Not Merged

The following features exist in upstream commits but were not merged. These represent functional enhancements that could be beneficial:

| Feature | Upstream Commit | Status | Notes |
|---------|----------------|--------|-------|
| Java 25 with Leyden | 9768ec6 | ⚠️ Not tested | Requires Java 25 runtime |
| Build memory optimization | 7c6d9c2 | ✅ Compatible | Experimental header feature |
| File access cleanup | b6bb396 | ✅ Compatible | Cleanup of file access requirements |
| Multiple exclusion tags | 5f526ab | ✅ Compatible | Enhanced exclusion tag support |
| ImportList exclusions | 1817243 | ✅ Compatible | New feature for *arr integration |
| Leaving-Soon folder structure | 825b573 | ✅ Compatible | Enhanced folder support for movies |
| Streamystats 2.4.0 | ed7c0d5 | ✅ Compatible | Updated Streamystats integration |

### Why Not Merge Everything?

1. **Preserve Fork Identity**: The Management UI and comprehensive documentation are the fork's main value propositions
2. **Maintain Stability**: Merging all changes risks breaking the fork-specific features
3. **Safer Defaults Philosophy**: Fork aims to be safer for new users
4. **Build Flexibility**: Dynamic repository owner is essential for fork maintenance

## Future Sync Strategy

### For Next Sync

1. **Source Code Review**: 
   - Check for new Kotlin files in upstream
   - Review changes to existing source files
   - Test compatibility with fork's Management UI

2. **Configuration Updates**:
   - Review `application-template.yml` changes
   - Keep `enabled: false` defaults
   - Adopt new configuration options where applicable

3. **Build System**:
   - Review dependency updates
   - Keep dynamic repository owner support
   - Test build with fork's CI/CD

4. **Documentation**:
   - Update fork documentation to reflect new upstream features
   - Maintain both English and Spanish versions
   - Update FORK_CHANGES.md with new differences

### Cherry-Pick Candidates

When syncing, consider cherry-picking these types of commits:

- ✅ Bug fixes
- ✅ New features (that don't conflict with fork-specific code)
- ✅ Dependency updates (tested for compatibility)
- ✅ Performance improvements
- ❌ Documentation changes (fork has its own)
- ❌ Default configuration changes (fork uses safer defaults)
- ❌ Build system changes (unless enhancing, not replacing)

## Testing Before Merge

Before merging upstream changes:

1. **Build Test**: Ensure project builds successfully
2. **Management UI Test**: Verify web interface still works
3. **Configuration Test**: Test with fork's default configuration
4. **Documentation Check**: Ensure docs remain accurate
5. **Image Build**: Test Docker image creation with fork's registry

## Summary

This sync analyzed upstream changes and determined that:

- ✅ Fork-specific features are valuable and should be preserved
- ✅ Upstream and fork have compatible source code
- ℹ️ Main differences are intentional (safer defaults, additional features)
- 📋 Several upstream features could be cherry-picked in future if needed
- ⚠️ Java 24+ is now required by both fork and upstream

**Recommendation**: Current state is acceptable. No immediate merge needed. Monitor upstream for critical bug fixes to cherry-pick.

---

Last Updated: October 3, 2025
