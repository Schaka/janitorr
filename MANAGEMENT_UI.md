# Janitorr Management UI

## Overview

The Janitorr Management UI provides a web-based interface to monitor and manually trigger cleanup functions. This allows administrators to:

- View the current system status and configuration
- Manually trigger any of the three cleanup schedules on-demand
- Monitor which cleanups have been executed
- See real-time feedback on cleanup operations

## Accessing the UI

Once Janitorr is running, access the management UI by navigating to:

```
http://<janitorr-host>:<port>/
```

For example, if running with the default port 8978:
```
http://localhost:8978/
```

## Configuration

### Enabling/Disabling the UI

The Management UI can be controlled through configuration:

**Via application.yml:**
```yaml
management:
  ui:
    enabled: true  # Set to false to disable the UI
```

**Via environment variable (recommended for Docker):**
```yaml
environment:
  - JANITORR_UI_ENABLED=true  # Set to false to disable
```

**Via Spring Boot command line:**
```bash
java -jar janitorr.jar --management.ui.enabled=true
```

### Configuration Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JANITORR_UI_ENABLED` | `true` | Enable/disable the Management UI |
| `SERVER_PORT` | `8080` | Internal port for the application |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | `health,info,management` | Which endpoints to expose |
| `SPRING_PROFILES_ACTIVE` | - | Spring Boot profiles to activate |

### Startup Logging

Janitorr logs the UI status on startup:

```
INFO - Management UI is ENABLED and available at http://localhost:8080/
INFO - Management API endpoints available at http://localhost:8080/api/management/
```

Or when disabled:
```
INFO - Management UI is DISABLED by configuration (management.ui.enabled=false)
```

### Docker Compose Example

**With UI enabled (default):**
```yaml
services:
  janitorr:
    image: ghcr.io/carcheky/janitorr:jvm-stable
    environment:
      - JANITORR_UI_ENABLED=true
    ports:
      - "8080:8080"  # Expose UI port
```

**With UI disabled:**
```yaml
services:
  janitorr:
    image: ghcr.io/carcheky/janitorr:jvm-stable
    environment:
      - JANITORR_UI_ENABLED=false
    # No need to expose port 8080 if UI is disabled
```

## Features

### System Status

The status section displays:

- **Dry Run Mode**: Shows if Janitorr is running in dry-run mode (no actual deletions)
- **Run Once Mode**: Indicates if Janitorr will exit after completing all cleanups
- **Media Deletion**: Status of media-based cleanup (enabled/disabled)
- **Tag-Based Deletion**: Status of tag-based cleanup (enabled/disabled)
- **Episode Deletion**: Status of episode cleanup (enabled/disabled)

Status is automatically refreshed every 30 seconds, or you can manually refresh using the "Refresh Status" button.

### Manual Cleanup Actions

Three cleanup actions are available:

#### 1. Media Cleanup
Cleans up movies and TV shows based on configured age and disk space thresholds.

#### 2. Tag-Based Cleanup
Cleans up media based on configured tags and expiration schedules.

#### 3. Episode Cleanup
Cleans up individual episodes of shows tagged for episode-based cleanup, based on age and maximum episode count.

Each action shows:
- A description of what the cleanup does
- The last run status (Completed/Not yet)
- A button to manually trigger the cleanup

### Execution Feedback

When you trigger a cleanup:
1. The button shows "Running..." and is disabled during execution
2. A success or error message appears at the bottom of the page
3. The status is automatically refreshed after completion
4. The "Last Run" status is updated

## API Endpoints

The UI communicates with these REST API endpoints:

### GET /api/management/status
Returns the current system status and configuration.

**Response:**
```json
{
  "dryRun": true,
  "runOnce": false,
  "mediaDeletionEnabled": true,
  "tagBasedDeletionEnabled": true,
  "episodeDeletionEnabled": false,
  "hasMediaCleanupRun": true,
  "hasTagBasedCleanupRun": false,
  "hasWeeklyEpisodeCleanupRun": false,
  "timestamp": 1234567890
}
```

### POST /api/management/cleanup/media
Manually triggers the media cleanup schedule.

**Response:**
```json
{
  "success": true,
  "message": "Media cleanup completed successfully",
  "timestamp": 1234567890
}
```

### POST /api/management/cleanup/tag-based
Manually triggers the tag-based cleanup schedule.

### POST /api/management/cleanup/episodes
Manually triggers the episode cleanup schedule.

## Notes

- Manual triggers execute the same cleanup logic as the scheduled runs
- All cleanup operations respect the configured dry-run mode
- Cleanup operations are logged in the Janitorr logs
- Multiple cleanup operations can be triggered in sequence, but each must complete before starting the next
- The UI is excluded from the "leyden" profile (used for AOT cache compilation)

### Important: Spring Boot Profiles

**The Management UI will not be available if the `leyden` profile is active at runtime.**

The `leyden` profile is only used during Docker image builds for AOT cache generation. If you encounter 404 errors when accessing the Management UI or API endpoints:

1. Check that `SPRING_PROFILES_ACTIVE` environment variable does NOT include `leyden`
2. Remove `leyden` from your docker-compose.yml if present
3. Restart the container

**Default behavior (recommended):** Do not set `SPRING_PROFILES_ACTIVE` at all, which ensures the Management UI is enabled.

## Security Considerations

- The management UI has no authentication by default
- Consider using a reverse proxy with authentication if exposing to the internet
- The UI only provides read access to configuration and the ability to trigger cleanups
- No configuration changes can be made through the UI (configuration is read-only)

## Troubleshooting

### UI Not Accessible

If you cannot access the Management UI:

1. **Check if UI is enabled:**
   ```bash
   docker logs janitorr | grep "Management UI"
   ```
   Should show: `Management UI is ENABLED`

2. **Verify the port:**
   - Default port is `8080`
   - Check your port mapping in docker-compose.yml
   - Verify `SERVER_PORT` environment variable if you changed it

3. **Check the profile:**
   - The UI is disabled when using the `leyden` profile
   - Verify `SPRING_PROFILES_ACTIVE` does not contain `leyden`

4. **Verify configuration:**
   ```bash
   docker exec janitorr cat /config/application.yml | grep -A 3 "management:"
   ```

### UI Shows 404 Error

If accessing the root URL shows a 404:

1. Verify the UI is not disabled in configuration
2. Check that static resources are available (should be in the JAR)
3. Check logs for startup errors

### API Endpoints Return 404

If `/api/management/status` returns 404:

1. Verify `JANITORR_UI_ENABLED` is set to `true`
2. Check that you're not using the `leyden` profile
3. Verify the controller is loaded by checking startup logs

### How to Disable the UI

To run Janitorr without the Management UI:

**Method 1 - Environment Variable:**
```yaml
environment:
  - JANITORR_UI_ENABLED=false
```

**Method 2 - Configuration File:**
```yaml
management:
  ui:
    enabled: false
```

After disabling, you'll see in logs:
```
INFO - Management UI is DISABLED by configuration (management.ui.enabled=false)
```

