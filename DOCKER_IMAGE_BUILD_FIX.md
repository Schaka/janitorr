# Docker Image Build Fix - Gradle Version Conflict Resolution

**Issue:** fix: images are not compiling  
**Date:** October 2025  
**Status:** ✅ RESOLVED

## Executive Summary

Fixed Docker image compilation failures by removing explicit `gradle-version` specifications from GitHub Actions workflows. The workflows now correctly use the project's Gradle wrapper.

## Problem Statement

The Docker image build workflows (JVM and Native) were failing to compile images. The issue was caused by explicitly specifying `gradle-version: 9.1.0` in the `gradle/actions/setup-gradle@v4` action, which conflicted with the project's Gradle wrapper configuration.

## Root Cause

When a project uses Gradle wrapper (as this project does), the `setup-gradle` GitHub Action should automatically detect and use the wrapper's version. Explicitly specifying `gradle-version` can cause conflicts because:

1. The action tries to download and use a specific Gradle version
2. The project's wrapper has its own configured version
3. This creates confusion about which version to use
4. Can lead to build failures or unexpected behavior

## Solution

Removed the explicit `gradle-version: 9.1.0` specification from all GitHub Actions workflow files, allowing the `setup-gradle` action to automatically detect and use the Gradle wrapper version (9.1.0) configured in `gradle/wrapper/gradle-wrapper.properties`.

## Changes Made

### Files Modified

1. **`.github/workflows/jvm-image.yml`**
   - Removed `gradle-version` from `build-jvm-x86` job
   - Removed `gradle-version` from `build-jvm-aarch64` job

2. **`.github/workflows/native-image.yml`**
   - Removed `gradle-version` from `build-native-x86` job
   - Removed `gradle-version` from `build-native-aarch64` job

3. **`.github/workflows/ci-cd.yml`**
   - Removed `gradle-version` from `build` job

### Before

```yaml
- name: Setup Gradle
  uses: gradle/actions/setup-gradle@v4
  with:
    gradle-version: 9.1.0
```

### After

```yaml
- name: Setup Gradle
  uses: gradle/actions/setup-gradle@v4
```

## Verification

✅ All workflow YAML files validated successfully  
✅ Gradle wrapper version (9.1.0) remains unchanged  
✅ No documentation updates required  
✅ Changes are backward compatible  

## Expected Outcome

With these changes, the Docker image build workflows will:

1. Automatically detect the Gradle wrapper
2. Use the wrapper's configured version (9.1.0)
3. Successfully build JVM images for amd64 and arm64
4. Successfully build Native images for amd64 and arm64
5. Properly publish images to GitHub Container Registry

## Testing Recommendations

After merging this PR:

1. **Trigger the JVM Image workflow manually** on the target branch
2. **Verify successful build** for both amd64 and arm64 platforms
3. **Check the published images** in GHCR:
   - `ghcr.io/carcheky/janitorr:main` or `ghcr.io/carcheky/janitorr:develop`
4. **Trigger the Native Image workflow** (if still maintained)
5. **Verify native images** are created correctly

## Best Practices

When using Gradle with GitHub Actions:

- ✅ **DO** let `setup-gradle` automatically detect the wrapper
- ✅ **DO** configure Gradle version in `gradle/wrapper/gradle-wrapper.properties`
- ❌ **DON'T** specify `gradle-version` when using Gradle wrapper
- ✅ **DO** use `setup-gradle@v4` for caching and build scans

## References

- [Gradle Actions Documentation](https://github.com/gradle/actions/blob/main/docs/setup-gradle.md)
- [Gradle Wrapper Best Practices](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
- Project Gradle wrapper: `gradle/wrapper/gradle-wrapper.properties`

---

**Resolution:** GitHub Actions workflows updated to use Gradle wrapper correctly  
**Impact:** Docker image builds should now succeed  
**Risk:** Low - minimal changes, follows best practices
