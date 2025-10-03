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
- The UI is excluded from the "leyden" profile (used for native image compilation)

## Security Considerations

- The management UI has no authentication by default
- Consider using a reverse proxy with authentication if exposing to the internet
- The UI only provides read access to configuration and the ability to trigger cleanups
- No configuration changes can be made through the UI (configuration is read-only)
