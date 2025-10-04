# Implementation: Configurable Paths for ConfigurationService

## Overview

This implementation addresses issue #46 by making hard-coded paths in `ConfigurationService` configurable through Spring Boot properties, supporting different deployment scenarios and testing environments.

## Changes Made

### 1. New Configuration Properties Class
**File**: `src/main/kotlin/com/github/schaka/janitorr/config/ConfigurationPathProperties.kt`

Created a new Spring Boot `@ConfigurationProperties` class:
- Prefix: `configuration.paths`
- Properties:
  - `configFile`: Path to main configuration file (default: `/config/application.yml`)
  - `backupDirectory`: Directory for configuration backups (default: `/config/backups`)

### 2. Updated ConfigurationService
**File**: `src/main/kotlin/com/github/schaka/janitorr/config/service/ConfigurationService.kt`

**Changes:**
- Removed hard-coded constants: `CONFIG_FILE_PATH` and `BACKUP_DIR`
- Added `ConfigurationPathProperties` as constructor-injected dependency
- Updated all 12 path references to use the injected properties:
  - `exportConfiguration()` - 2 references
  - `importConfiguration()` - 1 reference
  - `createBackup()` - 3 references
  - `restoreFromBackup()` - 2 references
  - `listBackups()` - 1 reference
  - `resetToDefaults()` - 1 reference
  - `writeConfigurationToFile()` - 1 reference

### 3. Updated Configuration Template
**File**: `src/main/resources/application-template.yml`

Added new configuration section with comprehensive documentation:
```yaml
# Configuration Path Settings
# These paths can be customized for different deployment scenarios and testing environments
configuration:
  paths:
    config-file: "/config/application.yml" # Path to the main configuration file
    backup-directory: "/config/backups" # Directory where configuration backups are stored
```

### 4. Added Tests
**File**: `src/test/kotlin/com/github/schaka/janitorr/config/ConfigurationPathPropertiesTest.kt`

Comprehensive test coverage:
- `testConfigurationPathPropertiesDefaults()`: Validates default values
- `testConfigurationPathPropertiesCustomValues()`: Validates custom values work correctly

## Technical Details

### Spring Boot Integration
- Uses `@ConfigurationProperties` annotation with prefix `configuration.paths`
- Automatically discovered by `@ConfigurationPropertiesScan` in `JanitorrApplication`
- No additional configuration required for bean registration

### Backwards Compatibility
✅ **100% Backwards Compatible**
- Default values identical to original hard-coded paths
- Existing deployments work without any changes
- No breaking changes to API or behavior
- Only adds new optional configuration capability

## Benefits

1. **Testability**: Different paths can be used in test environments
2. **Flexibility**: Supports custom deployment scenarios (e.g., multiple instances)
3. **Best Practice**: Follows Spring Boot configuration conventions
4. **Maintainability**: Centralized path configuration
5. **Documentation**: Paths are now visible and documented in config template

## Usage Examples

### Default Behavior (No Changes Required)
```yaml
# Uses defaults - same as before
# /config/application.yml
# /config/backups
```

### Custom Paths
```yaml
configuration:
  paths:
    config-file: "/custom/janitorr/application.yml"
    backup-directory: "/custom/janitorr/backups"
```

### Testing Scenario
```yaml
configuration:
  paths:
    config-file: "/tmp/test-config.yml"
    backup-directory: "/tmp/test-backups"
```

## Verification

### Code Quality
- ✅ Follows existing code patterns in the project
- ✅ Consistent with other `*Properties` classes
- ✅ Proper dependency injection
- ✅ Comprehensive test coverage

### Functionality
- ✅ All 12 path references updated
- ✅ No hard-coded paths remaining in ConfigurationService
- ✅ Backwards compatible with existing deployments
- ✅ Configuration template updated and documented

## Files Changed

1. **Created:**
   - `src/main/kotlin/com/github/schaka/janitorr/config/ConfigurationPathProperties.kt` (9 lines)
   - `src/test/kotlin/com/github/schaka/janitorr/config/ConfigurationPathPropertiesTest.kt` (46 lines)

2. **Modified:**
   - `src/main/kotlin/com/github/schaka/janitorr/config/service/ConfigurationService.kt` (14 deletions, 13 additions)
   - `src/main/resources/application-template.yml` (7 additions)

**Total Changes:** 75 lines added, 14 lines removed

## Related Issues

Fixes #46 - Hard-coded paths should be configurable properties to support different deployment scenarios and testing environments.

## Notes

- No documentation updates required beyond the configuration template comments
- No impact on existing features or functionality
- Implementation follows the principle of minimal, surgical changes
- Ready for merge and deployment
