# Docker Image Naming Convention Update

## Overview

This document describes the changes made to the Docker image naming convention for the `carcheky/janitorr` fork.

## Issue Reference

**Issue:** fix: no se están compilando las imágenes docker

**Objective:** Restructure the Docker image compilation to have two separate images with simplified tags:
- `janitorr` - JVM image with tags `:latest`, `:main`, `:develop`, `:1.0.0`
- `janitorr-native` - Native image with tags `:latest`, `:main`, `:develop`, `:1.0.0`

## Changes Made

### 1. GitHub Actions Workflows

#### `.github/workflows/jvm-image.yml`
**Changes:**
- Removed `jvm-` prefix from image tags
- Tags now: `ghcr.io/carcheky/janitorr:main`, `ghcr.io/carcheky/janitorr:develop`
- For version tags: `ghcr.io/carcheky/janitorr:latest` and `ghcr.io/carcheky/janitorr:{version}`
- Platform-specific images: `amd64-{branch}`, `arm64-{branch}` (instead of `jvm-amd64-{branch}`)

#### `.github/workflows/native-image.yml`
**Changes:**
- Changed image name from `janitorr` to `janitorr-native`
- Tags now: `ghcr.io/carcheky/janitorr-native:main`, `ghcr.io/carcheky/janitorr-native:develop`
- For version tags: `ghcr.io/carcheky/janitorr-native:latest` and `ghcr.io/carcheky/janitorr-native:{version}`
- Platform-specific images: `janitorr-native:amd64-{branch}`, `janitorr-native:arm64-{branch}`

### 2. Build Configuration

#### `build.gradle.kts`
**Changes:**
- Added `imageSuffix` variable: `-native` for native images, empty for JVM images
- Container image name now includes suffix: `ghcr.io/{owner}/janitorr{suffix}`
- Removed `imageType` from base tag, now just platform: `amd64` instead of `jvm-amd64`
- Tag format: `{platform}-{shortCommit}`, `{platform}-{branch}`

### 3. Documentation Updates

All documentation has been updated to reflect the new naming convention:

#### English Documentation
- `README.md` - Updated Docker Compose examples to use `:latest` and `janitorr-native:latest`
- `docs/CI-CD.md` - Updated image tagging tables and examples
- `docs/DOCKER_IMAGES_GUIDE.md` - Updated all image references
- `docs/DOCKER_IMAGE_VERIFICATION.md` - Updated verification examples
- `docs/PUBLISHING_DOCKER_IMAGES.md` - Updated publishing instructions
- `docs/WORKFLOW-DIAGRAM.md` - Updated Docker image tags table

#### Spanish Documentation
- `docs/wiki/es/Configuracion-Docker-Compose.md` - Updated all image references
- `docs/wiki/es/Preguntas-Frecuentes.md` - Updated FAQ entries

### 4. Scripts

#### `scripts/verify-images.sh`
**Changes:**
- Added `PACKAGE_NATIVE` variable for `janitorr-native`
- Updated `check_image_curl` function to accept package name parameter
- Updated all image checks to use new naming convention
- Added checks for both `janitorr` and `janitorr-native` packages

#### `scripts/README.md`
**Changes:**
- Updated examples to show new image tags
- Updated documentation to reflect new naming

## Image Tag Comparison

### Before (Old Convention)

| Branch/Tag | JVM Image | Native Image |
|------------|-----------|--------------|
| main | `janitorr:jvm-main` | `janitorr:native-main` |
| develop | `janitorr:jvm-develop` | `janitorr:native-develop` |
| v1.0.0 tag | `janitorr:jvm-stable`, `janitorr:latest`, `janitorr:1.0.0` | `janitorr:native-stable`, `janitorr:native-latest`, `janitorr:native-1.0.0` |

### After (New Convention)

| Branch/Tag | JVM Image | Native Image |
|------------|-----------|--------------|
| main | `janitorr:main` | `janitorr-native:main` |
| develop | `janitorr:develop` | `janitorr-native:develop` |
| v1.0.0 tag | `janitorr:latest`, `janitorr:1.0.0` | `janitorr-native:latest`, `janitorr-native:1.0.0` |

## Benefits

1. **Simpler for users**: Standard Docker convention (`:latest` is expected)
2. **Clear separation**: JVM and Native images are now distinct packages
3. **Less typing**: `:main` instead of `:jvm-main`
4. **Better organization**: Two separate image repositories in GHCR
5. **Backward compatibility not needed**: This is a fork-specific change

## Migration Guide for Users

If users were using the old tags (which were never published), they should update:

### Old Usage
```yaml
image: ghcr.io/carcheky/janitorr:jvm-stable
```

### New Usage
```yaml
image: ghcr.io/carcheky/janitorr:latest
```

### Old Native Usage
```yaml
image: ghcr.io/carcheky/janitorr:native-stable
```

### New Native Usage
```yaml
image: ghcr.io/carcheky/janitorr-native:latest
```

## Testing

To test the new configuration:

1. **Trigger JVM Image workflow manually** on the `main` or `develop` branch
2. **Verify the images are created** with the correct tags in GHCR:
   - `ghcr.io/carcheky/janitorr:main` or `ghcr.io/carcheky/janitorr:develop`
3. **Trigger Native Image workflow manually** on the `main` or `develop` branch
4. **Verify the native images are created** with the correct tags:
   - `ghcr.io/carcheky/janitorr-native:main` or `ghcr.io/carcheky/janitorr-native:develop`
5. **Create a version tag** (e.g., `v1.0.0`) and verify both workflows create:
   - `ghcr.io/carcheky/janitorr:latest` and `ghcr.io/carcheky/janitorr:1.0.0`
   - `ghcr.io/carcheky/janitorr-native:latest` and `ghcr.io/carcheky/janitorr-native:1.0.0`

## Validation

All changes have been validated:
- ✅ GitHub Actions workflow YAML syntax is valid
- ✅ Gradle build configuration syntax is valid
- ✅ Shell scripts syntax is valid
- ✅ Documentation is consistent across English and Spanish versions

## Files Modified

1. `.github/workflows/jvm-image.yml`
2. `.github/workflows/native-image.yml`
3. `build.gradle.kts`
4. `README.md`
5. `docs/CI-CD.md`
6. `docs/DOCKER_IMAGES_GUIDE.md`
7. `docs/DOCKER_IMAGE_VERIFICATION.md`
8. `docs/PUBLISHING_DOCKER_IMAGES.md`
9. `docs/WORKFLOW-DIAGRAM.md`
10. `docs/wiki/es/Configuracion-Docker-Compose.md`
11. `docs/wiki/es/Preguntas-Frecuentes.md`
12. `scripts/README.md`
13. `scripts/verify-images.sh`

## Next Steps

1. Merge this PR to the target branch
2. Manually trigger the workflows to build the first images with the new naming
3. Verify the images are published correctly in GHCR
4. Update any external documentation or references to the new image names

---

**Date:** October 4, 2025  
**Author:** GitHub Copilot Agent  
**Issue:** fix: no se están compilando las imágenes docker
