# Docker Image Tag Update Summary

## Overview

This update improves the Docker image tagging strategy to make it easier for users to use Janitorr images. The JVM images (recommended) now support simplified tags without the `jvm-` prefix, while native images retain the `native-` prefix for clarity.

## What Changed

### New Tagging Strategy

#### For JVM Images (Recommended)

**Branch Builds:**
- `ghcr.io/carcheky/janitorr:main` ← **NEW** simplified tag
- `ghcr.io/carcheky/janitorr:jvm-main` ← existing tag (still available)
- `ghcr.io/carcheky/janitorr:develop` ← **NEW** simplified tag
- `ghcr.io/carcheky/janitorr:jvm-develop` ← existing tag (still available)

**Stable Releases (from version tags like v1.9.0):**
- `ghcr.io/carcheky/janitorr:latest` ← **NEW** points to latest stable JVM image
- `ghcr.io/carcheky/janitorr:jvm-stable` ← existing tag (still available)
- `ghcr.io/carcheky/janitorr:v1.9.0` ← **NEW** version without prefix
- `ghcr.io/carcheky/janitorr:jvm-v1.9.0` ← existing tag (still available)

#### For Native Images (Deprecated)

**Branch Builds:**
- `ghcr.io/carcheky/janitorr:native-main` ← unchanged
- `ghcr.io/carcheky/janitorr:native-develop` ← unchanged

**Stable Releases:**
- `ghcr.io/carcheky/janitorr:native-latest` ← **NEW** points to latest stable native image
- `ghcr.io/carcheky/janitorr:native-stable` ← existing tag (still available)
- `ghcr.io/carcheky/janitorr:native-v1.9.0` ← existing tag (still available)

## Migration Guide

### If You're Using Stable Releases

**Before:**
```yaml
image: ghcr.io/carcheky/janitorr:jvm-stable
```

**After (recommended):**
```yaml
image: ghcr.io/carcheky/janitorr:latest
```

Both will work! The `:latest` tag is now an alias for `:jvm-stable` and makes it clearer that you're using the recommended JVM image.

### If You're Using Development Builds

**Before:**
```yaml
image: ghcr.io/carcheky/janitorr:jvm-main
```

**After (recommended):**
```yaml
image: ghcr.io/carcheky/janitorr:main
```

Again, both work! The simplified `:main` tag is easier to type and remember.

### If You're Using Native Images

**No changes required!** Native images keep their prefix to distinguish them from JVM images:

```yaml
image: ghcr.io/carcheky/janitorr:native-stable
# Or use the new alias:
image: ghcr.io/carcheky/janitorr:native-latest
```

## Backward Compatibility

✅ **All existing tags continue to work!** This update only adds new aliases; it doesn't remove any existing tags.

- `:jvm-stable`, `:jvm-main`, `:jvm-develop` → still work
- `:jvm-v1.9.0` style tags → still work
- All native image tags → unchanged

## Benefits

1. **Simpler for new users**: `:latest` is the standard Docker convention
2. **Less typing**: `:main` instead of `:jvm-main`
3. **Clear defaults**: Using `:latest` or `:main` gets you the recommended JVM image
4. **Better distinction**: Native images clearly marked with `native-` prefix
5. **Backward compatible**: All old tags still work

## Updated Documentation

The following documentation has been updated to reflect the new tagging strategy:

- ✅ English Docker Compose Setup Guide
- ✅ Spanish Docker Compose Setup Guide (Configuración Docker Compose)
- ✅ CI/CD Documentation
- ✅ Example Docker Compose files

## When Will This Take Effect?

The new tagging strategy will be active:

- ✅ Immediately for branch builds (when `main` or `develop` branches are updated)
- ✅ On the next release (when a new version tag like `v1.10.0` is created)

## Questions?

If you have questions about this change, please open a discussion on GitHub.

---

**Date**: January 2025  
**Issue**: #[issue number]  
**Related PR**: #[pr number]
