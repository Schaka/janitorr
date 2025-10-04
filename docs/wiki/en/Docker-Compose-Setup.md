# Docker Compose Setup

This guide will help you deploy Janitorr using Docker Compose.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration Steps](#configuration-steps)
- [Docker Compose Examples](#docker-compose-examples)
- [Volume Mapping](#volume-mapping)
- [Environment Variables](#environment-variables)
- [Health Checks](#health-checks)
- [Available Image Tags](#available-image-tags)
- [Full Stack Example](#full-stack-example)
- [Troubleshooting](#troubleshooting)

## Prerequisites

Before setting up Janitorr, ensure you have:

- Docker and Docker Compose installed
- A media server (Jellyfin or Emby)
- Media management tools (Sonarr and/or Radarr)
- Basic understanding of Docker volumes and networking

## Quick Start

1. **Create the configuration directory:**
   ```bash
   mkdir -p /appdata/janitorr/config
   mkdir -p /appdata/janitorr/logs
   ```

2. **Download the application template:**
   ```bash
   wget -O /appdata/janitorr/config/application.yml \
     https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
   ```

3. **Edit the configuration file:**
   ```bash
   nano /appdata/janitorr/config/application.yml
   ```
   Update with your *arr, Jellyfin/Emby, and Jellyseerr API keys.

4. **Create a `docker-compose.yml` file** (see examples below)

5. **Start Janitorr:**
   ```bash
   docker-compose up -d
   ```

6. **Access the Management UI:**
   Open `http://<your-server-ip>:<configured-port>/` in your browser
   
   **✅ The Management UI is fully functional!** You can now:
   - View system status in real-time
   - Manually trigger cleanup operations
   - Monitor cleanup execution

## Configuration Steps

### 1. Prepare the application.yml File

The `application.yml` file is **required** for Janitorr to start. Without it, the container will fail.

Download the template:
```bash
wget -O /appdata/janitorr/config/application.yml \
  https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
```

Key settings to configure:
- **API Keys**: Sonarr, Radarr, Jellyfin/Emby, Jellyseerr
- **Server URLs**: Point to your *arr services and media server
- **Port**: The port Janitorr will listen on (default: 8978)
- **Dry Run Mode**: Start with `dry-run: true` to test without deleting anything
- **Leaving Soon Directory**: Where symlinks for "leaving soon" media will be created

**✅ After configuration, the Management UI will be accessible at `http://localhost:8978/`**

### 2. Understand Volume Mapping

**Critical:** Volume paths must be consistent across all containers!

If Radarr stores movies at `/data/media/movies`, then:
- Janitorr must also see them at `/data/media/movies`
- Jellyfin must also access them at the same path (or you can use `media-server-leaving-soon-dir`)

#### Example Scenario

**Your host paths:**
- Movies: `/share_media/media/movies`
- TV Shows: `/share_media/media/tv`
- Leaving Soon: `/share_media/media/leaving-soon`

**Docker mappings for all containers:**
```yaml
volumes:
  - /share_media:/data
```

**In application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"
```

#### Special Case: Different Paths for Jellyfin

If Jellyfin sees the leaving-soon directory at a different path:

**Janitorr mapping:**
```yaml
volumes:
  - /share_media/media/leaving-soon:/data/media/leaving-soon
```

**Jellyfin mapping:**
```yaml
volumes:
  - /share_media/media/leaving-soon:/library/leaving-soon
```

**In application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"           # Path as Janitorr sees it
media-server-leaving-soon-dir: "/library/leaving-soon" # Path as Jellyfin sees it
```

### 3. Spring Boot Profiles Configuration

**Important:** Janitorr uses Spring Boot profiles for specific purposes. Understanding these is crucial for proper operation.

#### The `leyden` Profile

The `leyden` profile is **ONLY for build-time AOT cache generation** and should **NEVER be activated at runtime**. This profile:
- Disables the Management UI and API endpoints (`/api/management/*`)
- Is automatically used during Docker image builds
- **Must not be set** in your `SPRING_PROFILES_ACTIVE` environment variable

#### Setting Custom Profiles (Optional)

If you need to use custom Spring profiles for your own configuration, you can set them via environment variable:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod,custom  # Your custom profiles
```

**Warning:** Never include `leyden` in `SPRING_PROFILES_ACTIVE`. Doing so will disable the Management UI and cause 404 errors on `/api/management/*` endpoints.

#### Default Behavior

By default (when `SPRING_PROFILES_ACTIVE` is not set):
- ✅ Management UI is accessible at `http://<host>:<port>/`
- ✅ All API endpoints work correctly
- ✅ Scheduled cleanups run as configured

## Docker Compose Examples

### Basic Setup (JVM - Recommended)

```yaml
version: "3"

services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/carcheky/janitorr:jvm-stable
    user: 1000:1000 # Replace with your user ID
    mem_limit: 256M # Minimum 200M, recommended 256M
    mem_swappiness: 0
    volumes:
      - /appdata/janitorr/config/application.yml:/config/application.yml
      - /appdata/janitorr/logs:/logs
      - /share_media:/data
    environment:
      - THC_PATH=/health
      - THC_PORT=8081
      # IMPORTANT: Do NOT set SPRING_PROFILES_ACTIVE=leyden
      # The Management UI requires the leyden profile to be inactive
      # - SPRING_PROFILES_ACTIVE=prod  # Optional: your custom profiles only
    ports:
      - "8978:8978" # Optional: Only if you need external access
    healthcheck:
      test: ["CMD", "/workspace/health-check"]
      start_period: 30s
      interval: 5s
      retries: 3
    restart: unless-stopped
```

### Native Image Setup (Lower Memory Footprint)

> **Note:** The native image is deprecated as of v1.9.0. Use JVM image for better support.

```yaml
version: "3"

services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/carcheky/janitorr:native-stable
    user: 1000:1000
    volumes:
      - /appdata/janitorr/config/application.yml:/config/application.yml
      - /appdata/janitorr/logs:/logs
      - /share_media:/data
    environment:
      - THC_PATH=/health
      - THC_PORT=8081
      - SPRING_CONFIG_ADDITIONAL_LOCATION=/config/application.yml
    ports:
      - "8978:8978"
    healthcheck:
      test: ["CMD", "/workspace/health-check"]
      start_period: 30s
      interval: 5s
      retries: 3
    restart: unless-stopped
```

## Volume Mapping

### Required Volumes

1. **Configuration File:**
   ```yaml
   - /appdata/janitorr/config/application.yml:/config/application.yml
   ```
   Maps your configuration file into the container.

2. **Log Directory:**
   ```yaml
   - /appdata/janitorr/logs:/logs
   ```
   Stores logs on the host (enable file logging in application.yml).

3. **Media Directory:**
   ```yaml
   - /share_media:/data
   ```
   Must include all media directories that Sonarr/Radarr manage.

### Best Practices

- Use the **same volume mapping** for Janitorr, Sonarr, Radarr, and Jellyfin
- Ensure the user (`1000:1000`) has read/write permissions
- The leaving-soon directory must be writable (Janitorr creates symlinks here)

## Environment Variables

### Required Variables

- `THC_PATH=/health` - Health check endpoint path
- `THC_PORT=8081` - Health check port

### Optional Variables (Native Image Only)

- `SPRING_CONFIG_ADDITIONAL_LOCATION=/config/application.yml` - Config file location

### JVM Memory Configuration

The `mem_limit` is used to dynamically calculate the heap size:
- **Minimum:** 200M (may cause issues)
- **Recommended:** 256M
- **Large libraries:** 512M or higher

## Health Checks

Janitorr includes a built-in health checker:

```yaml
healthcheck:
  test: ["CMD", "/workspace/health-check"]
  start_period: 30s
  interval: 5s
  retries: 3
```

This ensures the container is healthy before routing traffic to it.

## Available Image Tags

### Stable Releases

#### JVM Images (Recommended)
- `ghcr.io/carcheky/janitorr:latest` - Latest stable JVM image (alias for jvm-stable)
- `ghcr.io/carcheky/janitorr:jvm-stable` - Latest stable JVM image (recommended)
- `ghcr.io/carcheky/janitorr:v1.x.x` - Specific version (e.g., v1.9.0)
- `ghcr.io/carcheky/janitorr:jvm-v1.x.x` - Specific JVM version with prefix

#### Native Images (Deprecated)
- `ghcr.io/carcheky/janitorr:native-latest` - Latest stable native image (deprecated)
- `ghcr.io/carcheky/janitorr:native-stable` - Latest stable native image (deprecated)
- `ghcr.io/carcheky/janitorr:native-v1.x.x` - Specific native version

### Development Builds

- `ghcr.io/carcheky/janitorr:main` - Latest main branch build (JVM)
- `ghcr.io/carcheky/janitorr:jvm-main` - Latest main branch build (JVM, with prefix)
- `ghcr.io/carcheky/janitorr:develop` - Latest development build (JVM)
- `ghcr.io/carcheky/janitorr:jvm-develop` - Latest development build (JVM, with prefix)
- `ghcr.io/carcheky/janitorr:native-main` - Latest main branch build (native)
- `ghcr.io/carcheky/janitorr:native-develop` - Latest development build (native)

> **Warning:** Development builds may be unstable. Use for testing only.

## Full Stack Example

Here's a complete Docker Compose setup with Jellyfin, Sonarr, Radarr, Jellyseerr, Jellystat, and Janitorr:

```yaml
version: "3"

services:
  jellyfin:
    image: jellyfin/jellyfin:latest
    container_name: jellyfin
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Berlin
    volumes:
      - /appdata/jellyfin:/config
      - /share_media:/data
    ports:
      - 8096:8096
    restart: unless-stopped

  radarr:
    image: lscr.io/linuxserver/radarr:latest
    container_name: radarr
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Berlin
    volumes:
      - /appdata/radarr:/config
      - /share_media:/data
    ports:
      - 7878:7878
    restart: unless-stopped

  sonarr:
    image: lscr.io/linuxserver/sonarr:latest
    container_name: sonarr
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Berlin
    volumes:
      - /appdata/sonarr:/config
      - /share_media:/data
    ports:
      - 8989:8989
    restart: unless-stopped

  jellyseerr:
    image: fallenbagel/jellyseerr:latest
    container_name: jellyseerr
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Berlin
    volumes:
      - /appdata/jellyseerr:/app/config
    ports:
      - 5050:5050
    restart: unless-stopped

  jellystat-db:
    container_name: jellystat-db
    image: postgres:15.2
    environment:
      POSTGRES_DB: 'jfstat'
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: mypassword
    volumes:
      - /appdata/jellystat/postgres-data:/var/lib/postgresql/data
    restart: unless-stopped

  jellystat:
    container_name: jellystat
    image: cyfershepard/jellystat:latest
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: mypassword
      POSTGRES_IP: jellystat-db
      POSTGRES_PORT: 5432
      JWT_SECRET: 'your-secret-here'
    volumes:
      - /appdata/jellystat/config:/app/backend/backup-data
    ports:
      - "3000:3000"
    depends_on:
      - jellystat-db
    restart: unless-stopped

  janitorr:
    container_name: janitorr
    image: ghcr.io/carcheky/janitorr:jvm-stable
    user: 1000:1000
    mem_limit: 256M
    mem_swappiness: 0
    volumes:
      - /appdata/janitorr/config/application.yml:/config/application.yml
      - /appdata/janitorr/logs:/logs
      - /share_media:/data
    environment:
      - THC_PATH=/health
      - THC_PORT=8081
    ports:
      - "8978:8978"
    healthcheck:
      test: ["CMD", "/workspace/health-check"]
      start_period: 30s
      interval: 5s
      retries: 3
    restart: unless-stopped
```

**Important Notes:**
- All containers use the same `/share_media:/data` mapping
- User IDs (1000:1000) should match your host user
- Update timezone (`TZ`) to match your location
- Change all passwords and secrets before deploying

## Troubleshooting

### Container Won't Start

**Problem:** Janitorr exits immediately after starting.

**Solution:**
1. Check if `application.yml` exists at the mapped location
2. Verify the file has correct permissions (readable by user 1000)
3. Check Docker logs: `docker logs janitorr`

### Cannot Create Symlinks

**Problem:** Janitorr logs show "Failed to create symlink" errors.

**Solution:**
1. Verify the leaving-soon directory is writable
2. Check volume mappings match between Janitorr and *arrs
3. Ensure the user has write permissions to the leaving-soon directory

### Files Not Being Deleted

**Problem:** Media isn't being deleted even though it should be.

**Solution:**
1. Check if dry-run mode is enabled in `application.yml`
2. Verify API keys are correct
3. Check that media has the proper age/requirements for deletion
4. Review Janitorr logs for any errors

### Port Already in Use

**Problem:** Docker says port 8978 is already allocated.

**Solution:**
1. Change the port mapping: `"8979:8978"` (host:container)
2. Or remove the ports section entirely if you don't need external access

### Permission Denied Errors

**Problem:** Janitorr can't read/write files.

**Solution:**
1. Check the `user:` setting matches your host user ID
2. Run `id` on your host to find your UID:GID
3. Update the `user:` field in docker-compose.yml
4. Set proper ownership: `chown -R 1000:1000 /appdata/janitorr /share_media`

### Management UI Returns 404 Errors

**✅ This issue has been FIXED in current releases!**

If you're still experiencing 404 errors, you're likely using an outdated image.

**Solution:**
1. Update to the latest image:
   ```yaml
   image: ghcr.io/carcheky/janitorr:jvm-stable
   ```
2. Pull the latest image:
   ```bash
   docker-compose pull janitorr
   ```
3. Restart the container:
   ```bash
   docker-compose up -d janitorr
   ```
4. Verify the endpoints are accessible:
   ```bash
   curl http://localhost:8978/api/management/status
   ```

**Expected behavior with current images:**
- ✅ `http://localhost:8978/` loads the Management UI
- ✅ `http://localhost:8978/api/management/status` returns JSON status
- ✅ All cleanup endpoints work correctly

## Next Steps

After successful deployment:

1. **Access the Management UI** at `http://<your-server-ip>:8978/`
   - ✅ **Working!** The UI is fully functional in all current releases
2. **Review the configuration** and verify all services are connected
3. **Test in dry-run mode** before enabling actual deletions
4. **Monitor the logs** to understand what Janitorr will do
5. **Set up the "Leaving Soon" collection** in Jellyfin
6. **Use the web interface** to trigger manual cleanups and monitor status

## Additional Resources

- [Configuration Guide](Configuration-Guide.md) - Detailed application.yml settings
- [FAQ](FAQ.md) - Common questions and answers
- [Troubleshooting](Troubleshooting.md) - Detailed troubleshooting guide
- [GitHub Discussions](https://github.com/carcheky/janitorr/discussions) - Community support

---

**Need help?** Check the [FAQ](FAQ.md) or start a [discussion](https://github.com/carcheky/janitorr/discussions)!
