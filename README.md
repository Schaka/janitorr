# Janitorr - Cleans up your media library

### Inspiration

This application is heavily inspired by [Maintainerr](https://github.com/jorenn92/Maintainerr).
If you're within the Plex ecosystem, want an easy to use GUI and more sophisticated functionality, you're better off
using it instead.

### Warning

Please use at your own risk.
You may enable dry-run mode. This is enabled in the config template by default.
Unless you disable dry-run mode, nothing will be deleted.

You may check the container logs for Janitorr to observe what the application would do, were you to turn off dry-run
mode.
If you don't manage your container via a GUI like portainer, try `docker logs janitorr`.

If you still don't trust Janitorr, you may enable Recycle Bin in the *arrs and disable Jellyfin/Emby.
This way, no deletes will be triggered on Jellyfin and everthing triggered in the *arrs will only go to the Recycle Bin.

To enable debug logging, add the following lines at the top of your `application.yml`:

```yml
logging:
  level:
    com.github.schaka: TRACE
```

### Introduction

**Janitorr** manages your media and cleans up after you.

- Do you hate being the janitor of your server?
- Do you have a lot of media that never gets watched?
- Do your users constantly request media, and let it sit there afterward never to be touched again?

You NEED [Maintainerr for Plex](https://github.com/jorenn92/Maintainerr) or Janitorr for Jellyfin and Emby.
It's THE solution for cleaning up your server and freeing up space before you run into issues.

## Features

- Dry-run mode to investigate changes before committing to any deletion
- Remote deletion, disk space aware deletion as well as tag based delete schedules supported
- Exclude items from deletion via tags in Sonarr/Radarr
- Configure expiration times for your media in Jellyfin, Emby, Jellyseerr, Radarr, and Sonarr
- Show a collection, containing rule matched media, on the Jellyfin home screen for a specific duration before deletion.
  Think "Leaving soon"
- Unmonitor and delete media from *arr
- Season by season removal for TV shows
- Clear requests from Jellyseerr and clean up leftover metadata in Jellyfin so no orphaned files are left

### Disclaimer

- **I don't use Emby. I implemented and tested it, but for maintenance I rely on bug reports**
- "Leaving Soon" Collections are *always* created and do not care for dry-run settings
- Jellyfin and Emby require user access to delete files, an API key is not enough - I recommend creating a user
  specifically for this task
- Jellyfin does NOT provide viewing stats like Plex, so we go by file age in the *arrs - Jellystat integration is planned
- Jellyfin/Emby and Jellyseerr are not required, but if you don't supply them, you may end up with orphaned folders,
  metadata, etc
- Only one of Jellyfin or Emby can be enabled at a time
- **If file system access isn't given, files currently still seeding may be deleted**

### Note to developers

I currently have to load pretty much the entire library in one REST call to manually match media. While both Jellyfin and Emby have
some (different) filters for your library's content,
I found both of them to be pretty wonky at best. Some parameters seemed to do nothing, others weren't marked as required
when they were or results were unpredictable when an invalid value was supplied.
This is also one area where Jellyfin and Emby tend to be quite different.

## Setup

Currently, the code is only published as a docker image
to [DockerHub](https://hub.docker.com/repository/docker/schaka/janitorr/general). If you cannot use Docker, you're out
of luck for now.

Depending on the configuration, files will be deleted if they are older than x days. Age is determined by your grab
history in the *arr apps. By default, it will choose the oldest file in the history. If any of your quality profiles allow for updates, it will
consider the most recent download when calculating its age.

To exclude media from being considered from deletion, set the `janitorr_keep` tag in Sonarr/Radarr. The actual tag
Janitorr looks for can be adjusted in your config file.

### Setting up Docker

- map /config from within the container to a host folder of your choice
- within that host folder, put a copy
  of [application.yml](https://github.com/Schaka/janitorr/blob/develop/src/main/resources/application-template.yml) from
  this repository
- adjust said copy with your own info like *arr, Jellyfin and Jellyseerr API keys and your preferred port

If using Jellyfin with filesystem access, ensure that Janitorr has access to the exact directory structure for the leaving-soon-dir as Jellyfin.
Additionally, make sure the *arrs directories are mapped the same way Janitorr into Janitorr as well.

Janitorr creates symlinks from whatever directory it receives from the arrs' API into the leaving-soon-dir.
If Radarr finds movies at `/data/media/movies` Janitorr needs to find them at `/data/media/movies` too. 
You need to ensure links can be created from the source (what's available in the arrs) to the destination (leaving-soon).
Since Janitorr creates the "Leaving Soon" collection for you with the path given in the config file, it needs to be accessible by Jellyfin.
If Janitorr thinks the directory can be found at `/somedir/leaving-soon`, Jellyfin needs to find it at `/somedir/leaving-soon` too.


### Docker config

Before using this, please make sure you've created the `application.yml` file and put it in the correct config directory
you intend to map.
The application requires it. You need to supply it, or Janitorr will not start correctly.
You don't have to publish ANY ports on the host machine.

An example of a `docker-compose.yml` may look like this:

```yml
version: '3'

services:
  janitorr:
    container_name: janitorr
    image: schaka/janitorr:latest
    ports:
      - 8978:8978 # Technically, we don't publish any endpoints, so this isn't strictly required
    volumes:
      - /appdata/janitorr/config:/config 
      - /share_media:/data
```

To get the latest build as found in the development branch, grab the following image: `schaka/janitorr:develop`.