# FAQ - Frequently Asked Questions

Common questions about Janitorr and their answers.

## General Questions

### What is Janitorr?

Janitorr is an automated media library management tool that works with Jellyfin/Emby and the *arr suite (Sonarr, Radarr). It helps you automatically clean up old, unwatched media to free up disk space.

### Is Janitorr safe to use?

Yes, when configured properly. We strongly recommend:
- Starting with `dry-run: true` enabled
- Reviewing logs before enabling deletions
- Enabling Recycle Bin in your *arr applications
- Testing with a small subset of media first

### Which media servers does Janitorr support?

Janitorr supports:
- Jellyfin (fully tested and maintained)
- Emby (implemented and tested, but relies on community bug reports for maintenance)

**Note:** Only one media server (Jellyfin OR Emby) can be enabled at a time.

### Do I need Jellyfin/Emby to use Janitorr?

No, but it's highly recommended. Without a media server configured:
- Janitorr can still delete files via the *arr applications
- However, you may end up with orphaned metadata folders
- The "Leaving Soon" collection feature won't work

## Setup Questions

### Where do I get the configuration file?

Download the template from the repository:
```bash
wget -O /appdata/janitorr/config/application.yml \
  https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
```

Then edit it with your specific settings (API keys, URLs, etc.).

### What are the minimum requirements?

- **Docker:** Version 20.10 or newer
- **Memory:** Minimum 200MB, recommended 256MB (512MB+ for large libraries)
- **Disk Space:** Minimal (a few MB for the application)
- **Required Services:**
  - At least one *arr application (Sonarr or Radarr)
  - Recommended: Jellyfin or Emby

### Why won't my container start?

Common reasons:
1. **Missing application.yml** - This file is required
2. **Incorrect permissions** - The user (1000:1000) must be able to read the config file
3. **Port conflict** - Another service is using port 8978
4. **Invalid YAML** - Check your application.yml for syntax errors

Check the logs: `docker logs janitorr`

### How do I check the logs?

**Via Docker:**
```bash
docker logs janitorr
```

**Via log files** (if file logging is enabled in application.yml):
```bash
tail -f /appdata/janitorr/logs/janitorr.log
```

## Configuration Questions

### What is dry-run mode?

Dry-run mode (`dry-run: true`) makes Janitorr simulate deletions without actually deleting anything. It will:
- Log what it would delete
- Create "Leaving Soon" collections (these are always created)
- NOT delete any files or metadata

This is perfect for testing and understanding what Janitorr will do.

### How do I prevent specific media from being deleted?

Use tags in Sonarr/Radarr:
1. In Sonarr/Radarr, create a tag called `janitorr_keep` (or customize in your config)
2. Apply this tag to any movie/series you want to keep permanently
3. Janitorr will skip any media with this tag

### How does Janitorr determine what to delete?

Janitorr uses multiple factors:
1. **Age:** Based on when it was grabbed (from *arr history)
2. **Watch history:** If Jellystat/Streamystats is configured, the most recent watch date overrides the grab date
3. **Disk space:** If configured, Janitorr only deletes when disk usage exceeds a threshold
4. **Tags:** Media with the configured exclusion tag is never deleted

### What are the different cleanup schedules?

Janitorr has three cleanup schedules:

1. **Media Cleanup** - Deletes movies and TV shows based on age and disk space
2. **Tag-Based Cleanup** - Deletes media based on configured tags and expiration times
3. **Episode Cleanup** - Deletes individual episodes based on age or maximum count (for weekly shows)

Each can be enabled/disabled and scheduled independently.

## Operation Questions

### What is the "Leaving Soon" collection?

The "Leaving Soon" collection is shown in Jellyfin/Emby before media is deleted. It:
- Gives users a warning that content will be removed soon
- Is created by symlinking files from your library
- Appears on the Jellyfin home screen
- Can be configured to show for X days before deletion

**Important:** This collection is ALWAYS created, even in dry-run mode.

### How do I manually trigger a cleanup?

Use the Management UI:
1. Navigate to `http://<your-server>:8978/`
2. Click the button for the cleanup you want to run
3. Monitor the status and logs

### Can I run Janitorr once and exit?

Yes! Set `run-once: true` in your application.yml. Janitorr will:
1. Perform all enabled cleanups
2. Exit automatically
3. This is useful for cron jobs or manual runs

### Does Janitorr delete after watching?

**No.** Janitorr does NOT delete media based solely on watch status. 

For that functionality, see [Jellyfin Media Cleaner](https://github.com/shemanaev/jellyfin-plugin-media-cleaner).

Janitorr deletes based on:
- Age (days since download or last watch)
- Disk space thresholds
- Tag-based schedules

## Troubleshooting Questions

### Why aren't files being deleted?

Common reasons:
1. **Dry-run mode is enabled** - Set `dry-run: false`
2. **Media doesn't meet age requirements** - Check your configured minimum days
3. **Disk space threshold not met** - If you have disk-space-aware deletion enabled
4. **Media has exclusion tag** - Check if the media has your configured keep tag
5. **Media wasn't downloaded by *arr** - Janitorr only manages media downloaded through Sonarr/Radarr

### Why can't Janitorr create symlinks?

Reasons:
1. **Volume mapping mismatch** - Ensure all containers see the same paths
2. **Permission issues** - The user needs write access to the leaving-soon directory
3. **Filesystem limitations** - Some filesystems don't support symlinks (rare with Linux)

Solution: Check logs and verify volume mappings are consistent.

### Why do I see "Path not found" errors?

This usually means:
- The *arr application reports a path that Janitorr can't access
- Volume mappings are different between containers

Example problem:
- Radarr sees movies at `/movies`
- Janitorr sees them at `/data/movies`

Solution: Use the same volume mapping for all containers.

### How do I update Janitorr?

```bash
cd /path/to/docker-compose.yml
docker-compose pull
docker-compose up -d
```

This will:
1. Download the latest image
2. Recreate the container with the new image
3. Preserve your configuration and logs

## Advanced Questions

### Can I use Janitorr with Plex?

No, Janitorr is designed for Jellyfin and Emby only. For Plex, consider using [Maintainerr](https://github.com/jorenn92/Maintainerr), which inspired Janitorr.

### Can I use multiple instances of Sonarr or Radarr?

Yes, configure multiple *arr instances in your application.yml. Janitorr will manage media from all configured instances.

### What's the difference between JVM and native images?

- **JVM Image** (`jvm-stable`): 
  - Recommended
  - Better supported
  - Slightly higher memory usage (~256MB)
  - Better long-term performance due to JIT optimization

- **Native Image** (`native-stable`):
  - Deprecated as of v1.9.0
  - Lower initial memory footprint
  - Faster startup
  - Limited support going forward

### Can I customize the cleanup schedules?

Yes, in application.yml you can configure:
- Cron expressions for each schedule
- Enable/disable each cleanup type
- Set different age thresholds
- Configure disk space thresholds

### How do I integrate with Jellystat or Streamystats?

Configure the connection details in application.yml. When connected, Janitorr will:
- Use watch history to determine media age
- Prefer the most recent watch date over the grab date
- This prevents deletion of recently watched media

**Note:** Only one statistics service (Jellystat OR Streamystats) can be enabled at a time.

## Still have questions?

- Check the [Troubleshooting](Troubleshooting.md) guide
- Review the [Configuration Guide](Configuration-Guide.md)
- Search [existing issues](https://github.com/carcheky/janitorr/issues)
- Start a [discussion](https://github.com/carcheky/janitorr/discussions)

---

[← Back to Home](Home.md)
