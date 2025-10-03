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

The default port is **8978** (configurable in `application.yml`).

### Common Access URLs

**Local access:**
```
http://localhost:8978/
```

**Network access (replace with your server IP):**
```
http://192.168.1.100:8978/
```

**Docker container name (if on same network):**
```
http://janitorr:8978/
```

### Verifying the UI is Running

You can verify Janitorr is running by checking the health endpoint:
```bash
curl http://localhost:8978/health
```

If you get a connection error, check:
1. The container is running: `docker ps | grep janitorr`
2. The port is correctly mapped in your docker-compose.yml
3. The port matches the one in application.yml (`server.port`)

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

**Example:**
```bash
curl http://localhost:8978/api/management/status
```

**Response Fields:**
- `dryRun` - Whether Janitorr is in dry-run mode (true = no actual deletions)
- `runOnce` - Whether Janitorr will exit after completing all cleanups
- `mediaDeletionEnabled` - Media cleanup is configured and enabled
- `tagBasedDeletionEnabled` - Tag-based cleanup is configured and enabled
- `episodeDeletionEnabled` - Episode cleanup is configured and enabled
- `hasMediaCleanupRun` - Media cleanup has been executed at least once
- `hasTagBasedCleanupRun` - Tag-based cleanup has been executed at least once
- `hasWeeklyEpisodeCleanupRun` - Episode cleanup has been executed at least once
- `timestamp` - Current server timestamp in milliseconds

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

**Example:**
```bash
curl -X POST http://localhost:8978/api/management/cleanup/media
```

### POST /api/management/cleanup/tag-based
Manually triggers the tag-based cleanup schedule.

**Example:**
```bash
curl -X POST http://localhost:8978/api/management/cleanup/tag-based
```

**Response:**
```json
{
  "success": true,
  "message": "Tag-based cleanup completed successfully",
  "timestamp": 1234567890
}
```

### POST /api/management/cleanup/episodes
Manually triggers the episode cleanup schedule.

**Example:**
```bash
curl -X POST http://localhost:8978/api/management/cleanup/episodes
```

**Response:**
```json
{
  "success": true,
  "message": "Episode cleanup completed successfully",
  "timestamp": 1234567890
}
```

### Error Responses

When an error occurs, all endpoints return:

```json
{
  "success": false,
  "message": "Error: <error details>",
  "timestamp": 1234567890
}
```

## Notes

- Manual triggers execute the same cleanup logic as the scheduled runs
- All cleanup operations respect the configured dry-run mode
- Cleanup operations are logged in the Janitorr logs
- Multiple cleanup operations can be triggered in sequence, but each must complete before starting the next
- The UI is excluded from the "leyden" profile (used for native image compilation)

## Troubleshooting

### UI Shows 404 Not Found

**Problem:** Navigating to `http://<host>:<port>/` returns a 404 error.

**Possible Causes:**
1. **Native image without UI**: The Management UI is excluded from the native image build
2. **Incorrect URL**: Make sure you're accessing the root path `/` not `/ui` or other paths
3. **Wrong port**: Verify the port matches your `application.yml` configuration

**Solutions:**
1. **If using native image**, switch to the JVM image:
   ```yaml
   image: ghcr.io/carcheky/janitorr:jvm-stable
   ```
   The native image (`native-stable`) does not include the Management UI.

2. **Verify port configuration** in `application.yml`:
   ```yaml
   server:
     port: 8978
   ```

3. **Check Docker port mapping** in docker-compose.yml:
   ```yaml
   ports:
     - "8978:8978"  # host:container
   ```

### Connection Refused / Cannot Connect

**Problem:** Browser shows "Connection refused" or "Unable to connect".

**Possible Causes:**
1. Janitorr container is not running
2. Port is not published/mapped correctly
3. Firewall blocking the port
4. Container failed to start

**Solutions:**

1. **Check if container is running:**
   ```bash
   docker ps | grep janitorr
   ```
   If not running, check logs:
   ```bash
   docker logs janitorr
   ```

2. **Verify port mapping** (should show port in output):
   ```bash
   docker port janitorr
   ```

3. **Check application is listening:**
   ```bash
   docker exec janitorr netstat -tulpn | grep 8978
   ```

4. **Test from inside the container:**
   ```bash
   docker exec janitorr curl http://localhost:8978/
   ```

5. **Common fixes:**
   - Ensure `ports:` section exists in docker-compose.yml
   - Restart the container: `docker-compose restart janitorr`
   - Check firewall rules: `sudo ufw status`

### API Endpoints Return Errors

**Problem:** API calls return error messages or HTTP error codes.

**Common Error: "Media cleanup failed"**

**Possible Causes:**
1. Invalid configuration (missing API keys, wrong URLs)
2. Cannot connect to *arr services or media server
3. Permission issues with media files
4. Dry-run mode preventing operations

**Solutions:**

1. **Check container logs:**
   ```bash
   docker logs janitorr --tail 100
   ```

2. **Enable debug logging** in `application.yml`:
   ```yaml
   logging:
     level:
       com.github.schaka: DEBUG
   ```

3. **Verify configuration:**
   - API keys are correct
   - Service URLs are accessible from the container
   - Paths are correctly mapped

4. **Test API connectivity manually:**
   ```bash
   # Test Sonarr connection (from inside container)
   docker exec janitorr curl http://sonarr:8989/api/v3/system/status \
     -H "X-Api-Key: YOUR_API_KEY"
   ```

### UI Loads But Buttons Don't Work

**Problem:** UI appears but clicking cleanup buttons does nothing or shows errors.

**Possible Causes:**
1. JavaScript errors in browser console
2. CORS issues if using a reverse proxy
3. API endpoints not responding

**Solutions:**

1. **Open browser developer console** (F12) and check for errors

2. **Test API directly:**
   ```bash
   curl http://localhost:8978/api/management/status
   ```
   Should return JSON with status information.

3. **If using reverse proxy**, ensure WebSocket and API paths are correctly proxied:
   ```nginx
   # Nginx example
   location / {
       proxy_pass http://janitorr:8978;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
   }
   ```

### Status Shows All Cleanups Disabled

**Problem:** UI shows all cleanup types as disabled.

**Possible Causes:**
1. Cleanups not enabled in configuration
2. Missing required configuration (API keys, URLs)

**Solutions:**

1. **Enable cleanups in `application.yml`:**
   ```yaml
   media-cleanup:
     enabled: true
     schedule: "0 0 2 * * ?"
   
   tag-cleanup:
     enabled: true
     schedule: "0 0 3 * * ?"
   
   episode-cleanup:
     enabled: true
     schedule: "0 0 4 * * ?"
   ```

2. **Verify required services are configured:**
   - At least one *arr service (Sonarr or Radarr)
   - Jellyfin or Emby (optional but recommended)

### Manual Cleanup Does Nothing

**Problem:** Triggering manual cleanup returns success but nothing happens.

**Possible Causes:**
1. Dry-run mode is enabled (expected behavior)
2. No media meets deletion criteria
3. All media is excluded via tags

**Solutions:**

1. **Check dry-run mode** in `application.yml`:
   ```yaml
   dry-run: true  # Change to false to enable actual deletions
   ```
   When `dry-run: true`, Janitorr will log what it *would* delete but won't actually delete anything.

2. **Check the logs** to see what Janitorr is doing:
   ```bash
   docker logs janitorr --tail 100
   ```
   Look for messages like "Would delete" (dry-run) or "Deleting" (actual deletion).

3. **Verify media meets deletion criteria:**
   - Media is older than `minimum-days` threshold
   - Media doesn't have exclusion tags (`janitorr_keep`)
   - Disk usage exceeds threshold (if disk-aware deletion is enabled)

## Security Considerations

- The management UI has no authentication by default
- Consider using a reverse proxy with authentication if exposing to the internet
- The UI only provides read access to configuration and the ability to trigger cleanups
- No configuration changes can be made through the UI (configuration is read-only)

## Docker Image Compatibility

### JVM Image (Recommended)

**Image:** `ghcr.io/carcheky/janitorr:jvm-stable`

- ✅ **Includes Management UI**
- ✅ Fully supported and maintained
- ✅ Better compatibility with Spring Boot features
- Requires more memory (minimum 200MB, recommended 256MB)
- Slower initial startup (10-30 seconds)

**Use this image for:**
- Production deployments
- Full feature access including the Management UI
- Best compatibility and support

### Native Image (Deprecated)

**Image:** `ghcr.io/carcheky/janitorr:native-stable`

- ❌ **Does NOT include Management UI**
- ⚠️ Deprecated as of v1.9.0
- Lower memory footprint (~100MB less)
- Faster startup time
- Limited Spring Boot feature support

**Important:** The native image excludes the Management UI due to GraalVM native image compilation limitations. If you need the UI, use the JVM image.

### Choosing the Right Image

| Feature | JVM Image | Native Image |
|---------|-----------|--------------|
| Management UI | ✅ Yes | ❌ No |
| Memory Usage | 256MB recommended | ~150MB |
| Startup Time | 10-30s | 2-5s |
| Status | ✅ Supported | ⚠️ Deprecated |
| Recommended | ✅ Yes | ❌ No |

**Migration from Native to JVM:**

If you're currently using the native image and want the Management UI:

1. Change the image in docker-compose.yml:
   ```yaml
   # Old (native)
   image: ghcr.io/carcheky/janitorr:native-stable
   
   # New (JVM)
   image: ghcr.io/carcheky/janitorr:jvm-stable
   ```

2. Remove `SPRING_CONFIG_ADDITIONAL_LOCATION` if present (not needed for JVM image)

3. Increase memory limit:
   ```yaml
   mem_limit: 256M  # Increase from lower value
   ```

4. Restart the container:
   ```bash
   docker-compose pull
   docker-compose up -d
   ```

5. Access the UI at `http://<host>:8978/`

## Environment Variables

The Management UI uses the same port as the main application.

**Required variables:**
```yaml
environment:
  - THC_PATH=/health    # Health check endpoint
  - THC_PORT=8081       # Health check port
```

**Port configuration:**

The UI port is configured in `application.yml`:
```yaml
server:
  port: 8978  # Change this to use a different port
```

Map it in docker-compose.yml:
```yaml
ports:
  - "8978:8978"  # host_port:container_port
```

To use a different host port (e.g., 9000) while keeping container port 8978:
```yaml
ports:
  - "9000:8978"  # Access UI at http://localhost:9000/
```

## Additional Resources

- [Configuration Guide](docs/wiki/en/Configuration-Guide.md) - Detailed configuration options
- [Docker Compose Setup](docs/wiki/en/Docker-Compose-Setup.md) - Complete deployment guide
- [Troubleshooting](docs/wiki/en/Troubleshooting.md) - General troubleshooting
- [FAQ](docs/wiki/en/FAQ.md) - Frequently asked questions
