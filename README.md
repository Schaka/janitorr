# Janitorr - Cleans up your media library

### Inspiration
This application is heavily inspired by [Maintainerr](https://github.com/jorenn92/Maintainerr).
If you're within the Plex ecosystem, want an easy to use GUI and more sophisticated functionality, you're better off using it instead.

### Warning
Please use at your own risk It's still undergoing testing.
You may enable dry-run mode. This is enabled in the config template by default.
Unless you disable dry-run mode, nothing will be deleted.

You may check the container logs for Janitorr to observe what the application would do, were you to turn off dry-run mode.
If you don't manage your container via a GUI like portainer, try `docker logs janitorr`

### Introduction
**Janitorr** manages your media and cleans up after you.

- Do you hate being the janitor of your server?
- Do you have a lot of media that never gets watched?
- Do your users constantly request media, and let it sit there afterward never to be touched again?

You NEED [Maintainerr for Plex](https://github.com/jorenn92/Maintainerr) or Janitorr for Jellyfin.
It's THE solution for cleaning up your server and freeing up space before you run into issues.

## Features
- Dry-run mode to investigate changes before committing to any deletion
- Configure expiration times for your media in Jellyfin, Jellyseerr, Radarr, and Sonarr
- Show a collection, containing rule matched media, on the Jellyfin home screen for a specific duration before deletion. Think "Leaving soon"
- Unmonitor and delete media from *arr
- Season by season removal for TV shows
- Clear requests from Jellyseerr and clean up leftover metadata in Jellyfin so no orphaned files are left

### Disclaimer
- "Leaving Soon" Collections are *always* created and do not care for dry-run settings
- Jellyfin requires user access to delete files, an API key is not enough - I recommend creating a user specifically for this task
- Jellyfin does NOT provide viewing stats like Jellyfin, so we go by file age
- Jellyfin and Jellyseerr are not required, but if you don't supply them, you may end up with orphaned folders, metadata, etc
- To disable Jellyfin/Jellyseerr, you need to entirely delete their client info from the config file or disable them via properties
- **If file system access isn't given, files currently still seeding may be deleted**

## Setup
Currently, the code is only published as a docker image to [DockerHub](https://hub.docker.com/repository/docker/schaka/janitorr/general). If you cannot use Docker, you're out of luck for now.

Depending on the configuration, files will be deleted if they are older than x days. Age is determined by your grab history in the *arr apps.
By default, it will choose the oldest file in the history. If any of your quality profiles allow for updates, it will consider the most recent download when calculating its age.

### Setting up Docker
- map /config from within the container to a host folder of your choice
- within that host folder, put a copy of [application.yml](https://github.com/Schaka/janitorr/blob/main/src/main/resources/application.yml) from this repository
- adjust said copy with your own info like *arr, jellyfin and jellyseerr API keys and your preferred port
- you do NOT need to fill in a torrent client YET 

If using Jellyfin with filesystem access, ensure that Janitorr has access to the exact directory structure as Jellyfin.
If Jellyfin finds its TV shows under `/data/media/tv` Janitorr needs the exact same mapping for its Docker container.

### Docker config
Before using this, please make sure you've created the `application.yml` file and put it in the correct config directory you intend to map.
The application requires it. You need to supply it, or Janitorr will not start correctly.
You don't have to publish ANY ports on the host machine.

An example of a `docker-compose.yml` may look like this:
```
version: '3'

services:
  janitorr:
    container_name: janitorr
    image: schaka/janitorr
    ports:
      - 8978:8978 # Technically, we don't publish any endpoints, so this isn't strictly required
    volumes:
      - /appdata/janitorr/config:/config 
      - /share_media:/data
```