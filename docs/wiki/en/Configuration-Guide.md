# Configuration Guide

This guide covers the configuration of Janitorr through the `application.yml` file.

## Overview

Janitorr is configured through a YAML file that must be provided when starting the container. The configuration file controls all aspects of Janitorr's behavior.

## Getting the Template

Download the configuration template:

```bash
wget -O /appdata/janitorr/config/application.yml \
  https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
```

Or manually download from: [application-template.yml](https://github.com/carcheky/janitorr/blob/main/src/main/resources/application-template.yml)

## Basic Configuration

### Essential Settings

These settings are required for Janitorr to function:

```yaml
# Server configuration
server:
  port: 8978  # Port Janitorr listens on

# Application behavior
dry-run: true     # IMPORTANT: Set to false to enable actual deletions
run-once: false   # Set to true to run once and exit

# Media paths
leaving-soon-dir: "/data/media/leaving-soon"
media-server-leaving-soon-dir: "/data/media/leaving-soon"
```

### Connecting to *arr Applications

Configure at least one *arr application:

```yaml
# Sonarr configuration
sonarr:
  - url: "http://sonarr:8989"
    api-key: "your-sonarr-api-key"
    
# Radarr configuration  
radarr:
  - url: "http://radarr:7878"
    api-key: "your-radarr-api-key"
```

You can configure multiple instances of each:

```yaml
sonarr:
  - url: "http://sonarr-4k:8989"
    api-key: "api-key-1"
  - url: "http://sonarr-1080p:8990"
    api-key: "api-key-2"
```

### Media Server Configuration

Configure either Jellyfin OR Emby (not both):

**Jellyfin:**
```yaml
jellyfin:
  enabled: true
  url: "http://jellyfin:8096"
  api-key: "your-jellyfin-api-key"
  username: "janitorr"      # User with delete permissions
  password: "your-password"
```

**Emby:**
```yaml
emby:
  enabled: true
  url: "http://emby:8096"
  api-key: "your-emby-api-key"
  username: "janitorr"
  password: "your-password"
```

**Note:** A user account (not just API key) is required to delete files.

## Advanced Configuration

### Request Management (Jellyseerr)

Clear requests for deleted media:

```yaml
jellyseerr:
  enabled: true
  url: "http://jellyseerr:5050"
  api-key: "your-jellyseerr-api-key"
```

### Statistics Integration

Configure Jellystat OR Streamystats (not both):

**Jellystat:**
```yaml
jellystat:
  enabled: true
  url: "http://jellystat:3000"
  api-key: "your-jellystat-api-key"
```

**Streamystats:**
```yaml
streamystats:
  enabled: true
  url: "http://streamystats:8080"
  api-key: "your-streamystats-api-key"
```

When configured, watch history will be used to determine media age instead of just grab date.

### Cleanup Schedules

#### Media Cleanup

Cleans movies and TV shows based on age and disk space:

```yaml
media-cleanup:
  enabled: true
  schedule: "0 0 2 * * ?"  # Cron: Daily at 2 AM
  minimum-days: 30          # Keep media for at least 30 days
  disk-threshold: 80        # Only clean when disk is 80% full (optional)
```

#### Tag-Based Cleanup

Delete media based on tags with custom expiration:

```yaml
tag-cleanup:
  enabled: true
  schedule: "0 0 3 * * ?"  # Cron: Daily at 3 AM
  tags:
    - name: "delete_90_days"
      days: 90
    - name: "delete_30_days"  
      days: 30
```

Create these tags in Sonarr/Radarr and apply them to media that should expire after the specified days.

#### Episode Cleanup

Clean individual episodes for shows tagged for episode-level management:

```yaml
episode-cleanup:
  enabled: true
  schedule: "0 0 4 * * ?"  # Cron: Daily at 4 AM
  episode-tag: "janitorr_episodes"  # Tag in Sonarr
  minimum-days: 7                    # Keep episodes for at least 7 days
  maximum-episodes: 10               # Keep max 10 unwatched episodes per show
```

Apply the `episode-tag` to TV shows where you want episode-level cleanup instead of removing entire shows.

### Exclusion Tags

Prevent specific media from being deleted:

```yaml
exclusion-tags:
  - "janitorr_keep"    # Default exclusion tag
  - "favorite"
  - "protected"
```

Any media with these tags in Sonarr/Radarr will never be deleted.

### Leaving Soon Collection

Configure the "Leaving Soon" collection shown in Jellyfin/Emby:

```yaml
leaving-soon:
  enabled: true
  days-before-deletion: 7   # Show in collection 7 days before deletion
  collection-name: "Leaving Soon"
```

**Important:** This collection is created even in dry-run mode.

### Logging

Configure logging behavior:

```yaml
logging:
  level:
    root: INFO
    com.github.schaka: DEBUG  # Change to DEBUG or TRACE for more detail
  file:
    name: /logs/janitorr.log
    max-size: 10MB
    max-history: 30
```

**Log Levels:**
- `ERROR` - Only errors
- `WARN` - Warnings and errors
- `INFO` - General information (recommended)
- `DEBUG` - Detailed debugging info
- `TRACE` - Very detailed trace information

## Management UI Configuration

The Management UI is enabled by default when using the JVM image.

### Port Configuration

The UI uses the same port as the main application:

```yaml
server:
  port: 8978  # Change to use a different port
```

### Accessing the UI

Once Janitorr is running:
```
http://<your-server-ip>:8978/
```

### Docker Port Mapping

Map the port in docker-compose.yml:
```yaml
ports:
  - "8978:8978"  # host:container
```

To use a different host port:
```yaml
ports:
  - "9000:8978"  # Access at http://localhost:9000/
```

### Image Requirements

⚠️ **Important:** The Management UI is only available in the JVM image.

```yaml
# ✅ Includes Management UI
image: ghcr.io/carcheky/janitorr:jvm-stable

# ❌ Does NOT include Management UI (deprecated)
image: ghcr.io/carcheky/janitorr:native-stable
```

For complete UI documentation, see [MANAGEMENT_UI.md](../../MANAGEMENT_UI.md).

## Path Configuration

### Understanding Path Mapping

**Critical:** Paths must be consistent across all containers!

#### Simple Setup (Recommended)

All containers use the same volume mapping:

**Docker Compose:**
```yaml
volumes:
  - /share_media:/data
```

**application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"
media-server-leaving-soon-dir: "/data/media/leaving-soon"
```

#### Complex Setup (Different Jellyfin Path)

When Jellyfin sees paths differently:

**Janitorr volumes:**
```yaml
volumes:
  - /share_media:/data
```

**Jellyfin volumes:**
```yaml
volumes:
  - /share_media/media/leaving-soon:/library/leaving-soon
```

**application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"           # As Janitorr sees it
media-server-leaving-soon-dir: "/library/leaving-soon" # As Jellyfin sees it
```

### Filesystem Access

When using filesystem access (not API-only):

```yaml
jellyfin:
  filesystem-access: true  # Use filesystem operations
```

This requires:
- Janitorr and Jellyfin have the exact same view of library paths
- The leaving-soon directory is accessible to both

## Disk Management

### Disk Space Aware Deletion

Only delete when disk usage exceeds a threshold:

```yaml
disk-management:
  enabled: true
  threshold: 85        # Only clean when disk is 85% full
  target: 70          # Clean until disk is 70% full
  path: "/data"       # Path to monitor
```

This prevents unnecessary deletions when you have plenty of space.

### Free Space Calculation

Configure how free space is calculated:

```yaml
free-space:
  buffer-gb: 100  # Always try to keep 100GB free
```

## Cron Schedule Examples

Cron format: `second minute hour day month weekday`

```yaml
# Every day at 2 AM
schedule: "0 0 2 * * ?"

# Every Sunday at 3 AM  
schedule: "0 0 3 ? * SUN"

# Every 6 hours
schedule: "0 0 */6 * * ?"

# First day of every month at midnight
schedule: "0 0 0 1 * ?"

# Weekdays at 2 AM
schedule: "0 0 2 ? * MON-FRI"
```

## Security Considerations

### API Keys

- Never commit API keys to version control
- Use strong, unique API keys for each service
- Rotate API keys periodically

### User Permissions

The Janitorr user needs:
- Read access to all media directories
- Write access to the leaving-soon directory
- Delete permissions in Jellyfin/Emby (requires user account)

### Network Access

If exposing the Management UI:
- Use a reverse proxy with authentication
- Consider using HTTPS
- Restrict access by IP if possible

### Management UI Access

The Management UI has no built-in authentication. To secure it:

**Option 1: No external access (recommended for most users)**
```yaml
# docker-compose.yml
ports:
  # Remove or comment out the ports section
  # - "8978:8978"  # Don't expose to host
```
Access the UI only from other containers on the same Docker network.

**Option 2: Reverse proxy with authentication**

Use Nginx, Traefik, or similar with basic auth:

```nginx
# Nginx example
location / {
    auth_basic "Janitorr Admin";
    auth_basic_user_file /etc/nginx/.htpasswd;
    proxy_pass http://janitorr:8978;
}
```

**Option 3: IP restriction**

Restrict access to specific IPs:
```nginx
location / {
    allow 192.168.1.0/24;  # Your local network
    deny all;
    proxy_pass http://janitorr:8978;
}
```

## Example Complete Configuration

See [Docker Compose Setup](Docker-Compose-Setup.md#full-stack-example) for a complete example with all services configured.

## Testing Your Configuration

1. **Start with dry-run enabled:**
   ```yaml
   dry-run: true
   ```

2. **Enable debug logging:**
   ```yaml
   logging:
     level:
       com.github.schaka: DEBUG
   ```

3. **Start Janitorr and check logs:**
   ```bash
   docker-compose up -d
   docker logs -f janitorr
   ```

4. **Verify connections:**
   - Check that Janitorr can connect to all *arr services
   - Verify Jellyfin/Emby connection
   - Confirm leaving-soon collection is created

5. **Review what would be deleted:**
   - Check logs for "Would delete" messages
   - Verify the media selected makes sense
   - Adjust age thresholds as needed

6. **Disable dry-run when ready:**
   ```yaml
   dry-run: false
   ```

## Configuration Validation

Common configuration mistakes:

❌ **Wrong:**
```yaml
leaving-soon-dir: /data/leaving-soon      # Missing quotes
dry-run: True                              # Capital T (should be lowercase)
```

✅ **Correct:**
```yaml
leaving-soon-dir: "/data/leaving-soon"
dry-run: true
```

## Troubleshooting

### Configuration Not Loading

- Verify the file is at the correct path
- Check YAML syntax (indentation, colons, quotes)
- Review container logs for parsing errors

### Services Not Connecting

- Verify URLs are accessible from inside the container
- Check API keys are correct
- Ensure services are on the same Docker network or accessible by IP

### Paths Not Found

- Verify volume mappings are correct
- Check that paths in application.yml match container paths
- Ensure permissions are correct

## Next Steps

- [Docker Compose Setup](Docker-Compose-Setup.md) - Complete deployment guide
- [FAQ](FAQ.md) - Common questions
- [Troubleshooting](Troubleshooting.md) - Detailed problem solving
- [Management UI Guide](../../MANAGEMENT_UI.md) - Web interface documentation

---

[← Back to Home](Home.md)
