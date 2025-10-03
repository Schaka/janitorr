# Web-Based Configuration Guide

## Overview

Janitorr now provides a comprehensive web-based configuration interface that allows you to manage all settings through your browser without editing YAML files manually.

## Accessing the Configuration UI

1. Navigate to Janitorr's Management UI at `http://<your-server>:8978/`
2. Click the **⚙️ Configuration** button
3. You'll be taken to the configuration interface with multiple tabs

## Interface Sections

### 🔌 Services Tab

Configure all external service connections:

#### Sonarr (TV Shows)
- **URL**: The base URL of your Sonarr instance (e.g., `http://sonarr:8989`)
- **API Key**: Your Sonarr API key (found in Settings → General → Security)
- **Delete Empty Shows**: Remove show entries from Sonarr when all seasons are deleted
- **Import Exclusions**: Add deleted media to Sonarr's import exclusion list
- **Determine Age By**: Choose how to calculate media age (Most Recent, Oldest, or Auto from profile)
- **🔍 Test Connection**: Verify connection to Sonarr

#### Radarr (Movies)
- **URL**: The base URL of your Radarr instance (e.g., `http://radarr:7878`)
- **API Key**: Your Radarr API key
- **Only Delete Files**: ⚠️ NOT RECOMMENDED - Deletes files but keeps Radarr entries
- **Import Exclusions**: Add deleted media to Radarr's import exclusion list
- **Determine Age By**: Choose how to calculate media age
- **🔍 Test Connection**: Verify connection to Radarr

#### Jellyfin/Emby Media Server
⚠️ Only one media server can be enabled at a time

- **URL**: Media server URL (e.g., `http://jellyfin:8096`)
- **API Key**: Media server API key
- **Username/Password**: Credentials for deletion operations
- **Allow Deletion**: Enable Janitorr to delete media from the server
- **Leaving Soon Type**: Choose what media types to include (Movies and TV, Movies Only, TV Only, None)
- **Collection Names**: Customize "Leaving Soon" collection names
- **🔍 Test Connection**: Verify connection to media server

#### Jellyseerr/Overseerr
- **URL**: Request management system URL (e.g., `http://jellyseerr:5055`)
- **API Key**: Jellyseerr/Overseerr API key
- **Match Server**: Enable for multiple Sonarr/Radarr instances
- **🔍 Test Connection**: Verify connection

#### Jellystat/Streamystats
⚠️ Only one statistics service can be enabled at a time

- **URL**: Statistics service URL (e.g., `http://jellystat:3000`)
- **API Key**: Statistics service API key
- **Whole TV Show**: Consider entire show watched if any episode is watched
- **🔍 Test Connection**: Verify connection

#### Bazarr (Optional)
- **URL**: Bazarr URL for subtitle management
- **API Key**: Bazarr API key
- **🔍 Test Connection**: Verify connection

**Tip**: Use the **🔍 Test All Connections** button at the top to test all enabled services at once.

---

### 🧹 Cleanup Tab

Configure automatic cleanup rules:

#### Media Deletion
Automatically delete movies and TV shows based on disk space and age.

- **Enable Media Deletion**: Turn on automatic cleanup
- **Movie Expiration Rules**: Define disk space % → days mappings
  - Example: `10% → 30 days` means movies older than 30 days are deleted when disk is 10% free or less
  - Higher percentages = more aggressive cleanup
- **Season Expiration Rules**: Same as movies but for TV show seasons
- **+ Add Rule**: Create additional expiration rules

#### Tag-Based Deletion
Delete media based on tags assigned in Sonarr/Radarr.

- **Enable Tag-Based Deletion**: Turn on tag-based cleanup
- **Minimum Free Disk %**: Only delete when disk space is below this threshold
- **Tag Schedules**: Configure tag → expiration mappings
  - Example: Tag `demo` → `7 days` means media tagged with "demo" expires after 7 days
- **+ Add Schedule**: Create additional tag schedules

#### Episode Deletion
Manage episodes of specific shows (typically for daily shows).

- **Enable Episode Deletion**: Turn on episode cleanup
- **Tag**: Shows with this tag in Sonarr will have episodes managed
- **Max Episodes to Keep**: Number of most recent episodes to retain
- **Max Age (days)**: Maximum age for any episode, even if under the max count

---

### 📁 File System Tab

Configure file system access and paths:

- **Enable File System Access**: Allow Janitorr to access the file system
- **Validate Seeding**: Check if files are still seeding before deletion
- **Rebuild From Scratch**: Clean and rebuild "Leaving Soon" directory each run
- **Leaving Soon Directory (Janitorr)**: Path as seen by Janitorr
- **Leaving Soon Directory (Media Server)**: Path as seen by Jellyfin/Emby (if different)
- **Free Space Check Directory**: Directory to check for free disk space (default: `/`)

**Important**: Paths must be consistent between Janitorr, media servers, and *arr services.

---

### ⚡ General Tab

Configure application-wide settings:

#### Application Behavior
- **Dry Run Mode**: 🔴 RECOMMENDED - Preview deletions without actually deleting
- **Run Once and Exit**: Run cleanup once then stop the application
- **Whole TV Show Mode**: Treat entire show as recently watched if any episode is watched
- **Whole Show Seeding Check**: Check if any season is seeding before deleting entire show
- **Leaving Soon Warning (days)**: Days before deletion to add media to "Leaving Soon" collection

#### Exclusion Tags
Tags that protect media from being deleted. Media with these tags in Sonarr/Radarr will never be cleaned up.

- Default: `janitorr_keep`
- **+ Add Tag**: Create additional exclusion tags

#### Management UI
- **Enable Management UI**: Turn this web interface on/off

---

### 💾 Backup Tab

Manage configuration backups:

#### Actions
- **Create Backup Now**: Manually create a backup of current configuration
- **📥 Export Configuration**: Download current configuration as YAML file
- **📤 Import Configuration**: Upload and apply a configuration YAML file
- **🔄 Reset to Defaults**: Restore configuration to template defaults

#### Available Backups
Lists all available configuration backups with timestamps. Each backup can be restored with the **Restore** button.

**Note**: Backups are created automatically before any configuration changes.

---

## Workflow

### First-Time Setup

1. **Configure Services Tab**
   - Enter URLs and API keys for all your services
   - Use **🔍 Test Connection** buttons to verify each service
   - Click **🔍 Test All Connections** to verify all at once

2. **Configure Cleanup Rules**
   - Start with conservative rules (longer expiration times)
   - Enable **Dry Run Mode** in General tab
   - Test rules and adjust as needed

3. **Set File System Paths**
   - Ensure paths match your Docker volume mappings
   - Verify paths are accessible by Janitorr

4. **Save Configuration**
   - Click **💾 Save Configuration**
   - ⚠️ Restart Janitorr for changes to take effect

### Making Changes

1. Modify any settings in the web UI
2. Test connections if changing service configurations
3. Click **💾 Save Configuration**
4. Restart Janitorr container for changes to take effect

### Testing Before Production

1. Enable **Dry Run Mode** in General tab
2. Save configuration and restart
3. Review logs to see what would be deleted
4. Adjust rules as needed
5. When satisfied, disable Dry Run Mode
6. Save and restart

---

## Important Notes

### Dry Run Mode
**Always test with Dry Run enabled first!** This allows you to see what Janitorr would delete without actually deleting anything.

### Restart Required
Configuration changes require a Janitorr restart to take effect. This is because Spring Boot loads configuration at startup.

```bash
docker restart janitorr
```

### Backups
- Backups are created automatically before any configuration changes
- Backups are stored in `/config/backups/` inside the container
- You can restore any previous backup from the Backup tab

### Path Consistency
Ensure paths are consistent across:
- Janitorr container
- Jellyfin/Emby container  
- Sonarr/Radarr containers

If Janitorr sees `/data/media/movies` and Jellyfin sees `/media/movies`, they won't match correctly.

### Connection Testing
Always test connections after entering new service credentials. This helps identify:
- Invalid URLs
- Incorrect API keys
- Network connectivity issues
- Authentication problems

### Security
The Management UI has no built-in authentication. If exposing to the internet, use a reverse proxy with authentication (e.g., Nginx, Traefik with Basic Auth).

---

## Troubleshooting

### Configuration Not Saving
- Check Janitorr has write permissions to `/config/application.yml`
- Check container logs for errors
- Verify volume mapping is correct

### Connection Tests Failing
- Verify URLs are accessible from Janitorr container
- Check API keys are correct
- Ensure services are running
- Check network configuration (Docker networks, firewall rules)

### Changes Not Taking Effect
- Did you restart Janitorr after saving?
- Check container logs for configuration errors
- Verify changes were saved to `/config/application.yml`

### UI Not Loading
- Verify Management UI is enabled: `management.ui.enabled=true`
- Check you're not using the `leyden` profile at runtime
- Check browser console for JavaScript errors

---

## Advanced Usage

### Importing/Exporting Configurations

**Export:**
1. Go to Backup tab
2. Click **📥 Export Configuration**
3. Save the downloaded `application.yml` file

**Import:**
1. Have a valid `application.yml` file ready
2. Go to Backup tab
3. Click **📤 Import Configuration**
4. Select your YAML file
5. Restart Janitorr

Use cases:
- Sharing configurations between instances
- Version control of configurations
- Quick disaster recovery

### Multiple Instances
If running multiple Janitorr instances:
1. Export configuration from primary instance
2. Import to secondary instances
3. Adjust instance-specific settings (ports, paths)
4. Save and restart

---

## Next Steps

After configuring Janitorr:
1. ✅ Test with Dry Run enabled
2. ✅ Review logs to verify behavior
3. ✅ Adjust rules based on your needs
4. ✅ Disable Dry Run when confident
5. ✅ Set up scheduled cleanups (cron schedules in YAML)

For more information, see:
- [Configuration Guide](Configuration-Guide.md) - Detailed YAML configuration reference
- [Docker Compose Setup](Docker-Compose-Setup.md) - Container deployment guide
- [FAQ](FAQ.md) - Common questions and answers
- [Troubleshooting](Troubleshooting.md) - Solving common issues
