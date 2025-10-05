# Docker Image Naming Verification - Issue Resolution

**Issue:** buid: ferificar namig de imágenes docker (build: verify naming of docker images)

**Date:** October 5, 2025  
**Status:** ✅ RESOLVED

## Executive Summary

This document details the resolution of inconsistent Docker image naming references found in the repository documentation. All active documentation has been updated to use the current, standardized naming convention.

## Problem Statement

Several documentation files contained outdated Docker image naming references that did not match the current naming convention established in `DOCKER_IMAGE_NAMING_UPDATE.md`. This caused confusion for users and provided incorrect guidance to Copilot agents.

## Current Naming Convention

As established in previous updates and documented in `DOCKER_IMAGE_NAMING_UPDATE.md` and `docs/DOCKER_IMAGES_GUIDE.md`:

### JVM Images (Recommended)
- **Package Name:** `janitorr`
- **Registry:** `ghcr.io/carcheky/janitorr`
- **Tags:**
  - `:latest` - Latest stable release
  - `:main` - Latest build from main branch
  - `:develop` - Latest build from develop branch
  - `:1.x.x` - Specific version (e.g., `:1.0.0`)

**Example:** `ghcr.io/carcheky/janitorr:latest`

### Native Images (Deprecated as of v1.9.0)
- **Package Name:** `janitorr-native`
- **Registry:** `ghcr.io/carcheky/janitorr-native`
- **Tags:**
  - `:latest` - Latest stable release
  - `:main` - Latest build from main branch
  - `:develop` - Latest build from develop branch
  - `:1.x.x` - Specific version (e.g., `:1.0.0`)

**Example:** `ghcr.io/carcheky/janitorr-native:latest`

## Outdated References Found and Fixed

### 1. `.github/copilot-instructions.md`

**Lines Modified:** 125-126, 425, 459-462

**Changes:**
- ❌ `ghcr.io/carcheky/janitorr:jvm-stable` → ✅ `ghcr.io/carcheky/janitorr:latest`
- ❌ `ghcr.io/carcheky/janitorr:native-stable` → ✅ `ghcr.io/carcheky/janitorr-native:latest`
- ❌ `ghcr.io/carcheky/janitorr:jvm-latest` → ✅ `ghcr.io/carcheky/janitorr:latest`
- ❌ Tag descriptions using `jvm-*` prefix → ✅ Clean tag names (`:latest`, `:main`, `:develop`, `:1.x.x`)

**Impact:** Copilot agents now receive correct instructions about Docker image naming.

### 2. `MANAGEMENT_UI.md`

**Lines Modified:** 94, 111, 346, 364

**Changes:**
- ❌ `ghcr.io/carcheky/janitorr:jvm-stable` → ✅ `ghcr.io/carcheky/janitorr:latest`
- ❌ `jvm-main` → ✅ `main`

**Impact:** Users see correct Docker Compose examples and troubleshooting guidance.

### 3. `docs/wiki/en/Troubleshooting.md`

**Line Modified:** 465

**Changes:**
- ❌ `ghcr.io/carcheky/janitorr:native-stable` → ✅ `ghcr.io/carcheky/janitorr-native:latest`

**Impact:** English troubleshooting guide now shows correct native image name.

### 4. `docs/wiki/es/Solucion-Problemas.md`

**Line Modified:** 465

**Changes:**
- ❌ `ghcr.io/carcheky/janitorr:native-stable` → ✅ `ghcr.io/carcheky/janitorr-native:latest`

**Impact:** Spanish troubleshooting guide now shows correct native image name.

### 5. `docs/WORKFLOW-DIAGRAM.md`

**Lines Modified:** 64, 73

**Changes:**
- ❌ `(jvm-stable, native-stable)` → ✅ `(latest, janitorr-native:latest)`
- ❌ `(jvm-develop, native-develop)` → ✅ `(develop, janitorr-native:develop)`

**Impact:** CI/CD workflow documentation now accurately reflects image tagging strategy.

## Historical Documentation (Preserved)

The following files contain old naming references but were intentionally left unchanged as they document historical changes:

- `DOCUMENTATION_UPDATE_SUMMARY.md` - Documents past documentation updates
- `docs/ISSUE_RESOLUTION_DOCKER_IMAGES.md` - Documents past issue resolution
- `PULL_REQUEST_SUMMARY.md` - Documents past pull request
- `DOCKER_IMAGE_TAG_UPDATE.md` - Documents historical tag migration
- `DOCKER_IMAGE_NAMING_UPDATE.md` - Documents the naming convention change

These files serve as a historical record and should not be modified.

## Verification

### Consistency Checks Performed

✅ **Workflows:** `.github/workflows/jvm-image.yml` and `native-image.yml` already use correct naming  
✅ **Scripts:** `scripts/verify-images.sh` already uses correct naming  
✅ **Build Config:** `build.gradle.kts` already uses correct naming  
✅ **Documentation:** All active documentation now uses correct naming  
✅ **Bilingual:** Both English and Spanish documentation updated  

### Commands Used for Verification

```bash
# Search for old naming references (excluding historical docs)
grep -r "jvm-stable\|jvm-latest\|native-stable" \
  --include="*.md" --include="*.yml" --include="*.yaml" \
  . 2>/dev/null | \
  grep -v "DOCKER_IMAGE_NAMING_UPDATE.md" | \
  grep -v "DOCKER_IMAGE_TAG_UPDATE.md" | \
  grep -v "DOCUMENTATION_UPDATE_SUMMARY.md" | \
  grep -v "docs/ISSUE_RESOLUTION" | \
  grep -v "PULL_REQUEST_SUMMARY.md"
# Result: No matches found
```

### Current State Verification

```bash
# JVM Workflow
grep -A3 "tags:" .github/workflows/jvm-image.yml
# Output shows: janitorr:$branch, janitorr:latest, janitorr:$version

# Native Workflow
grep -A3 "tags:" .github/workflows/native-image.yml
# Output shows: janitorr-native:$branch, janitorr-native:latest, janitorr-native:$version

# Verify Script
grep "check_image_curl" scripts/verify-images.sh
# Output shows correct package names and tags
```

## Benefits

1. **User Experience:** Users now see consistent image naming across all documentation
2. **Reduced Confusion:** Clear, simple tag names without prefixes
3. **Standards Compliance:** Follows Docker best practices (`:latest` for stable releases)
4. **Maintainability:** Easier to understand and maintain documentation
5. **AI Assistance:** Copilot agents receive correct instructions
6. **Bilingual Consistency:** Both EN and ES documentation aligned

## Migration Impact

**Breaking Changes:** None  
**User Action Required:** None (purely documentation cleanup)  
**Backward Compatibility:** N/A (documentation only)

Users who were already using the correct tags (`:latest`, `:main`, `:develop`) are unaffected. Users who may have followed old documentation will now see the correct references.

## Related Documentation

- [DOCKER_IMAGE_NAMING_UPDATE.md](../DOCKER_IMAGE_NAMING_UPDATE.md) - Original naming convention change
- [docs/DOCKER_IMAGES_GUIDE.md](docs/DOCKER_IMAGES_GUIDE.md) - Comprehensive image guide
- [docs/CI-CD.md](docs/CI-CD.md) - CI/CD pipeline documentation
- [scripts/verify-images.sh](scripts/verify-images.sh) - Image verification script

## Resolution Summary

| Aspect | Status |
|--------|--------|
| Documentation Consistency | ✅ Complete |
| Bilingual Updates | ✅ Complete |
| Workflow Alignment | ✅ Verified |
| Script Alignment | ✅ Verified |
| User Impact | ✅ Positive |
| Breaking Changes | ✅ None |

**All documentation now uses consistent, correct Docker image naming that matches the established convention and actual build configuration.**

---

**Resolved:** October 5, 2025  
**Verified by:** GitHub Copilot Coding Agent  
**Commit:** docs: fix docker image naming references to match current convention
