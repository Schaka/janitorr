# Troubleshooting

Common issues and their solutions when running Janitorr.

## Table of Contents

- [Container Issues](#container-issues)
- [Configuration Issues](#configuration-issues)
- [Connection Issues](#connection-issues)
- [File Operation Issues](#file-operation-issues)
- [Deletion Issues](#deletion-issues)
- [Performance Issues](#performance-issues)
- [Management UI Issues](#management-ui-issues)
- [Logging and Debugging](#logging-and-debugging)

## Container Issues

### Container Won't Start

**Symptoms:** Container exits immediately, won't stay running

**Common Causes:**
1. Missing or inaccessible `application.yml`
2. Invalid YAML syntax
3. Port already in use
4. Insufficient memory

**Solutions:**

1. **Check if configuration exists:**
   ```bash
   ls -la /appdata/janitorr/config/application.yml
   ```

2. **Verify file permissions:**
   ```bash
   # Should be readable by user 1000
   chmod 644 /appdata/janitorr/config/application.yml
   chown 1000:1000 /appdata/janitorr/config/application.yml
   ```

3. **Check logs:**
   ```bash
   docker logs janitorr
   ```

4. **Validate YAML syntax:**
   Use an online YAML validator or:
   ```bash
   docker run --rm -v /appdata/janitorr/config:/config \
     alpine sh -c "apk add --no-cache yq && yq eval /config/application.yml"
   ```

### Container Restarts Repeatedly

**Symptoms:** Container keeps restarting, health check failing

**Solutions:**

1. **Check memory limits:**
   ```yaml
   mem_limit: 256M  # Increase if needed
   ```

2. **Review health check logs:**
   ```bash
   docker inspect janitorr | grep -A 10 Health
   ```

3. **Disable health check temporarily:**
   ```yaml
   # Comment out or remove healthcheck section
   # healthcheck:
   #   test: ["CMD", "/workspace/health-check"]
   ```

### Port Conflict

**Symptoms:** Error: "port is already allocated"

**Solutions:**

1. **Find what's using the port:**
   ```bash
   sudo netstat -tulpn | grep 8978
   # or
   sudo lsof -i :8978
   ```

2. **Change the host port:**
   ```yaml
   ports:
     - "8979:8978"  # Use 8979 on host instead
   ```

3. **Remove port mapping** (if you don't need external access):
   ```yaml
   # Comment out or remove ports section
   # ports:
   #   - "8978:8978"
   ```

## Configuration Issues

### Invalid Configuration Syntax

**Symptoms:** Container logs show YAML parsing errors

**Solutions:**

1. **Common YAML mistakes:**
   ```yaml
   # ❌ Wrong: Missing quotes for strings with special chars
   leaving-soon-dir: /data/leaving-soon
   
   # ✅ Correct: Use quotes
   leaving-soon-dir: "/data/leaving-soon"
   ```

   ```yaml
   # ❌ Wrong: Inconsistent indentation
   sonarr:
     - url: "http://sonarr:8989"
       api-key: "key"
   ```

   ```yaml
   # ✅ Correct: Consistent indentation (2 spaces)
   sonarr:
     - url: "http://sonarr:8989"
       api-key: "key"
   ```

2. **Validate the YAML:**
   ```bash
   # Online: Use yamllint.com or similar
   # CLI: Install yamllint
   yamllint /appdata/janitorr/config/application.yml
   ```

### Configuration Not Taking Effect

**Symptoms:** Changes to application.yml don't apply

**Solutions:**

1. **Restart the container:**
   ```bash
   docker-compose restart janitorr
   ```

2. **Verify the file is mapped correctly:**
   ```bash
   docker exec janitorr cat /config/application.yml
   ```

3. **Check for override environment variables:**
   Some settings can be overridden by environment variables in docker-compose.yml

## Connection Issues

### Cannot Connect to *arr Applications

**Symptoms:** Logs show connection refused or timeout errors for Sonarr/Radarr

**Solutions:**

1. **Verify the URLs:**
   ```bash
   # From inside the container
   docker exec janitorr wget -O- http://sonarr:8989/api/v3/system/status
   ```

2. **Check if containers are on the same network:**
   ```bash
   docker network inspect bridge
   ```

3. **Use IP address instead of hostname:**
   ```yaml
   sonarr:
     - url: "http://192.168.1.10:8989"  # Use IP instead of hostname
   ```

4. **Verify API keys:**
   - Log into Sonarr/Radarr
   - Go to Settings → General
   - Copy the API key exactly

### Cannot Connect to Jellyfin/Emby

**Symptoms:** Authentication errors or connection failures

**Solutions:**

1. **Verify username and password:**
   ```yaml
   jellyfin:
     username: "janitorr"  # Must be exact
     password: "your-password"
   ```

2. **Check user exists and has permissions:**
   - User must exist in Jellyfin/Emby
   - User needs delete permissions
   - Test login manually at the Jellyfin/Emby web interface

3. **API key alone is not enough:**
   Janitorr needs both API key AND user credentials to delete files.

### Cannot Connect to Jellyseerr

**Symptoms:** Request cleanup fails

**Solutions:**

1. **Verify API key:**
   - Go to Jellyseerr Settings → General
   - Copy API key (under "API Key" section)

2. **Check URL format:**
   ```yaml
   jellyseerr:
     url: "http://jellyseerr:5050"  # No trailing slash
   ```

## File Operation Issues

### Cannot Create Symlinks

**Symptoms:** Logs show "Failed to create symlink" errors

**Causes:**
- Volume mapping mismatch
- Permission issues
- Filesystem doesn't support symlinks

**Solutions:**

1. **Verify paths are accessible:**
   ```bash
   docker exec janitorr ls -la /data/media/movies
   docker exec janitorr ls -la /data/media/leaving-soon
   ```

2. **Check write permissions:**
   ```bash
   # From host
   ls -la /share_media/media/leaving-soon
   # Should be writable by user 1000
   chown -R 1000:1000 /share_media/media/leaving-soon
   ```

3. **Ensure volume mappings match:**
   All containers (Janitorr, Sonarr, Radarr, Jellyfin) should use the same mapping:
   ```yaml
   volumes:
     - /share_media:/data
   ```

4. **Test symlink creation manually:**
   ```bash
   docker exec janitorr ln -s /data/media/movies/test.mkv /data/media/leaving-soon/test.mkv
   ```

### Path Not Found Errors

**Symptoms:** Logs show file or directory not found

**Cause:** Path reported by *arr doesn't exist in Janitorr's view

**Solutions:**

1. **Check volume mappings:**
   ```yaml
   # ❌ Wrong: Different mappings
   # Sonarr:
   volumes:
     - /media:/data
   
   # Janitorr:
   volumes:
     - /media:/movies  # MISMATCH!
   ```

   ```yaml
   # ✅ Correct: Same mappings
   # Both containers:
   volumes:
     - /media:/data
   ```

2. **Verify the path reported by *arr:**
   - Check Sonarr/Radarr → Series/Movie → Files
   - Note the path shown
   - Ensure Janitorr can see the same path

3. **Debug with logs:**
   Enable TRACE logging:
   ```yaml
   logging:
     level:
       com.github.schaka: TRACE
   ```

### Permission Denied Errors

**Symptoms:** Cannot read/write files, permission errors in logs

**Solutions:**

1. **Check user/group ID:**
   ```bash
   # On your host, find your user ID
   id
   # Example output: uid=1000(myuser) gid=1000(myuser)
   ```

2. **Update docker-compose.yml:**
   ```yaml
   user: 1000:1000  # Match your host user
   ```

3. **Fix file ownership:**
   ```bash
   sudo chown -R 1000:1000 /appdata/janitorr
   sudo chown -R 1000:1000 /share_media
   ```

4. **Check directory permissions:**
   ```bash
   # Directories need execute permission
   sudo chmod -R 755 /share_media
   ```

## Deletion Issues

### Files Not Being Deleted

**Symptoms:** Media that should be deleted remains

**Solutions:**

1. **Check if dry-run is enabled:**
   ```yaml
   dry-run: false  # Must be false to actually delete
   ```

2. **Verify media meets age requirements:**
   Check logs to see why media was skipped:
   ```bash
   docker logs janitorr 2>&1 | grep -i "skip\|keep\|too young"
   ```

3. **Check for exclusion tags:**
   ```bash
   # In Sonarr/Radarr, check if media has:
   # - janitorr_keep tag
   # - Any tag configured in exclusion-tags
   ```

4. **Verify cleanup is enabled:**
   ```yaml
   media-cleanup:
     enabled: true  # Must be true
   ```

5. **Check disk threshold:**
   If disk-aware deletion is enabled:
   ```yaml
   disk-management:
     enabled: true
     threshold: 80  # Only cleans when disk > 80%
   ```
   Deletion won't happen until disk usage exceeds the threshold.

6. **Media not from *arr:**
   Janitorr only manages media downloaded through Sonarr/Radarr. Manually added media won't be deleted.

### Deletions Happening Too Aggressively

**Symptoms:** Too much media is being deleted

**Solutions:**

1. **Increase minimum-days:**
   ```yaml
   media-cleanup:
     minimum-days: 60  # Keep media longer
   ```

2. **Add exclusion tags:**
   Tag media you want to keep permanently:
   ```yaml
   exclusion-tags:
     - "janitorr_keep"
     - "favorite"
   ```

3. **Adjust disk threshold:**
   ```yaml
   disk-management:
     threshold: 90  # Only clean when very full
   ```

4. **Review in dry-run mode first:**
   ```yaml
   dry-run: true
   ```
   Check logs to see what would be deleted.

### Leaving Soon Collection Not Showing

**Symptoms:** Collection doesn't appear in Jellyfin

**Solutions:**

1. **Verify collection is enabled:**
   ```yaml
   leaving-soon:
     enabled: true
   ```

2. **Check Jellyfin library:**
   - Go to Jellyfin → Libraries
   - Scan library
   - Collections should appear under "Collections"

3. **Verify symlinks were created:**
   ```bash
   ls -la /share_media/media/leaving-soon
   ```

4. **Check leaving-soon-dir path:**
   ```yaml
   leaving-soon-dir: "/data/media/leaving-soon"
   media-server-leaving-soon-dir: "/data/media/leaving-soon"  # Jellyfin's view
   ```

5. **Jellyfin library must include leaving-soon directory:**
   - In Jellyfin, go to Dashboard → Libraries
   - Your library must include the leaving-soon path

## Performance Issues

### High Memory Usage

**Symptoms:** Container using too much RAM

**Solutions:**

1. **Increase memory limit:**
   ```yaml
   mem_limit: 512M  # Or higher for large libraries
   ```

2. **Reduce scan frequency:**
   ```yaml
   media-cleanup:
     schedule: "0 0 2 * * ?"  # Less frequent
   ```

3. **Use native image** (if JVM is too heavy):
   ```yaml
   image: ghcr.io/carcheky/janitorr:native-stable
   ```
   Note: Native image is deprecated but uses less memory.

### Slow Scans

**Symptoms:** Cleanup takes a long time to complete

**Causes:**
- Large library
- Slow network to *arr services
- Many API calls

**Solutions:**

1. **Enable parallel processing** (if available in your version)

2. **Reduce API call frequency:**
   - Use statistics integration (Jellystat) to reduce *arr API calls

3. **Schedule during off-peak hours:**
   ```yaml
   media-cleanup:
     schedule: "0 0 3 * * ?"  # 3 AM when server is less busy
   ```

## Management UI Issues

**✅ Note:** The Management UI is fully functional in current releases. Most common issues have been resolved.

### UI Not Accessible

**Symptoms:** Cannot access the web UI at `http://localhost:8978/`

**Solutions:**

1. **Verify you're using the latest image:**
   ```bash
   docker-compose pull janitorr
   docker-compose up -d janitorr
   ```

2. **Check if UI is enabled:**
   ```bash
   docker logs janitorr | grep "Management UI"
   ```
   
   Should show:
   ```
   INFO - Management UI is ENABLED and available at http://localhost:8978/
   ```

2. **Verify environment variable:**
   ```bash
   docker exec janitorr printenv | grep JANITORR_UI_ENABLED
   ```
   
   Should return `JANITORR_UI_ENABLED=true` (or nothing, as true is the default)

3. **Check port mapping:**
   ```yaml
   ports:
     - "8978:8978"  # Make sure this is in your docker-compose.yml
   ```

4. **Test the endpoint:**
   ```bash
   curl http://localhost:8978/api/management/status
   ```
   
   Should return JSON with system status.

### UI Shows 404 Error

**✅ This issue has been FIXED in current releases!**

If you're seeing 404 errors on the Management UI:

**Symptoms:** Accessing root URL returns 404 Not Found

**Solution:**

1. **Update to the latest image:**
   ```bash
   docker-compose pull janitorr
   docker-compose up -d janitorr
   ```
   
2. **Verify the image tag:**
   ```yaml
   image: ghcr.io/carcheky/janitorr:latest  # Use this or :main for development
   ```

3. **Clear browser cache and retry**

**Expected behavior with current images:**
- ✅ `http://localhost:8978/` shows the Management UI
- ✅ All buttons and features work correctly
- ✅ API endpoints return proper responses

### API Endpoints Return 404

**✅ This issue has been FIXED in current releases!**

If `/api/management/status` returns 404:

**Solution:**
1. **Update to the latest image** as described above
2. **Verify the endpoint:**
   ```bash
   curl http://localhost:8978/api/management/status
   ```
3. **Check container logs:**
   ```bash
   docker logs janitorr | grep "Management"
   ```

### UI Features Not Working

**Symptoms:** Buttons don't respond or cleanups don't trigger

**Solutions:**

1. **Check browser console for errors:**
   - Open browser developer tools (F12)
   - Look for JavaScript errors in the Console tab

2. **Verify API connectivity:**
   ```bash
   curl -X POST http://localhost:8978/api/management/cleanup/media
   ```

3. **Check Janitorr logs:**
   ```bash
   docker logs -f janitorr
   ```
   Watch for cleanup execution messages.

### Disabling the UI

If you want to run Janitorr without the web UI:

**Method 1 - Environment Variable (recommended):**
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

After disabling, logs will show:
```
INFO - Management UI is DISABLED by configuration (management.ui.enabled=false)
```

## Logging and Debugging

### Enable Debug Logging

```yaml
logging:
  level:
    root: INFO
    com.github.schaka: DEBUG  # or TRACE for even more detail
  file:
    name: /logs/janitorr.log
```

### View Logs

**Container logs:**
```bash
docker logs janitorr
docker logs -f janitorr  # Follow logs in real-time
docker logs --tail 100 janitorr  # Last 100 lines
```

**File logs:**
```bash
tail -f /appdata/janitorr/logs/janitorr.log
grep -i error /appdata/janitorr/logs/janitorr.log
```

### Common Log Messages

**INFO level:**
- `Starting Janitorr` - Application starting
- `Connected to Sonarr` - Successful connection
- `Would delete` (dry-run) - What would be deleted
- `Deleted` - Actual deletion performed

**WARNING level:**
- `Skipping media - too young` - Media doesn't meet age requirements
- `API rate limit` - Too many API calls

**ERROR level:**
- `Failed to connect` - Connection issues
- `Permission denied` - File permission issues
- `Failed to delete` - Deletion failed

### Debug Checklist

When things aren't working:

1. ✅ Check container is running: `docker ps | grep janitorr`
2. ✅ Check logs: `docker logs janitorr`
3. ✅ Verify configuration: `docker exec janitorr cat /config/application.yml`
4. ✅ Test network connectivity: `docker exec janitorr ping sonarr`
5. ✅ Check file permissions: `docker exec janitorr ls -la /data`
6. ✅ Enable debug logging
7. ✅ Check disk space: `df -h`
8. ✅ Review *arr history for media in question

## Getting Help

If you've tried the above and still have issues:

1. **Check existing issues:**
   [GitHub Issues](https://github.com/carcheky/janitorr/issues)

2. **Search discussions:**
   [GitHub Discussions](https://github.com/carcheky/janitorr/discussions)

3. **Create a new discussion:**
   Include:
   - Your docker-compose.yml (remove sensitive data)
   - Relevant application.yml sections (remove API keys)
   - Container logs
   - What you've already tried

4. **Report a bug:**
   If you've found a bug, [open an issue](https://github.com/carcheky/janitorr/issues/new) with:
   - Detailed description
   - Steps to reproduce
   - Expected vs actual behavior
   - Logs showing the error

---

[← Back to Home](Home.md) | [Configuration Guide](Configuration-Guide.md) | [FAQ](FAQ.md)
