# Janitorr - Cleans up your media library

<p align="center">
    <img src="logos/janitorr_icon.png" width=384>
</p>

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
- Configure expiration times for your media in the *arrs and optionally, if media can be found in JellyStat
- Show a collection, containing rule matched media, on the Jellyfin home screen for a specific duration before deletion. Think: "Leaving soon"
- Unmonitor and delete media from *arr
- Season by season removal for TV shows, or optionally the entire show
- Clear requests from Jellyseerr and clean up leftover metadata in Jellyfin so no orphaned files are left

### Disclaimer

- **I don't use Emby. I implemented and tested it, but for maintenance I rely on bug reports**
- "Leaving Soon" Collections are *always* created and do not care for dry-run settings
- Jellyfin and Emby require user access to delete files, an API key is not enough - I recommend creating a user specifically for this task
- Jellyfin does NOT provide viewing stats like Plex, so we go by file age in the *arrs - unless you provide access to JellyStat
- Jellyfin/Emby and Jellyseerr are not required, but if you don't supply them, you may end up with orphaned folders,  metadata, etc
- Only one of Jellyfin or Emby can be enabled at a time
- **If file system access isn't given, files currently still seeding may be deleted by the Sonarr/Radarr**

### Troubleshooting
Before you create a new issue, please check previous issues to make sure nobody has faced the same problem before.
The Wiki also contains a troubleshooting section with commons errors.

### Note to developers

I currently have to load pretty much the entire library in one REST call to manually match media. While both Jellyfin and Emby have
some (different) filters for your library's content,
I found both of them to be pretty wonky at best. Some parameters seemed to do nothing, others weren't marked as required
when they were or results were unpredictable when an invalid value was supplied.
This is also one area where Jellyfin and Emby tend to be quite different.

For those more familiar with Java/Kotlin, GraalVM and Spring:
The reason the code looks a little messy and doesn't let Spring's magic run wild with `@ConditonalOnProperty` is because native images don't support this (yet).
Proxies are very limited and creating a `@Bean` inside a `@Config` doesn't produce working proxies for things like `@PostConstruct` and `@Cacheable` half the time.
AOT also doesn't work exactly the same as native image deployment and thus is a lot harder to debug.

## Setup

Currently, the code is only published as a docker image to [GitHub](https://github.com/Schaka/janitorr/pkgs/container/janitorr).
If you cannot use Docker, you'll have to compile it yourself from source.

Depending on the configuration, files will be deleted if they are older than x days. Age is determined by your grab
history in the *arr apps. By default, it will choose the oldest file in the history. You can determine if you want to calculate age by oldest or most recent download.
If Jellystat is set up, the most recent watch date overwrites the grab history, if it exists.

To exclude media from being considered from deletion, set the `janitorr_keep` tag in Sonarr/Radarr. The actual tag
Janitorr looks for can be adjusted in your config file.

### Setting up Docker

- follow the mapping for `application.yml` examples below
- within that host folder, put a copy of [application.yml](https://github.com/Schaka/janitorr/blob/develop/src/main/resources/application-template.yml) from this repository
- adjust said copy with your own info like *arr, Jellyfin and Jellyseerr API keys and your preferred port

If using Jellyfin with filesystem access, ensure that Janitorr has access to the exact directory structure for the leaving-soon-dir as Jellyfin.
Additionally, make sure the *arrs directories are mapped the same way Janitorr into Janitorr as well.

Janitorr creates symlinks from whatever directory it receives from the arrs' API into the `leaving-soon-dir`.
If Radarr finds movies at `/data/media/movies` Janitorr needs to find them at `/data/media/movies` too.
You need to ensure links can be created from the source (in the arrs' library) to the destination (leaving-soon).
By default, both `media-server-leaving-soon-dir` and `leaving-soon-dir` should be identical if your volume mappings are identical.


If Janitorr's mapping looks like this:
`/share_media/media/leaving-soon:/data/media/leaving-soon`

And Jellyfin's like this:
`/share_media/media/leaving-soon:/library/leaving-soon`

Then your `application.yml` should look like:
```
leaving-soon-dir: "/data/media/leaving-soon"
media-server-leaving-soon-dir: "/library/leaving-soon"
```

**You may also check out [this example](examples/example-compose.yml) of a full stack setup.**

### Docker config

Before using this, please make sure you've created the `application.yml` file and put it in the correct config directory you intend to map.
The application requires it. You need to supply it, or Janitorr will not start correctly.
You don't have to publish ANY ports on the host machine.

An example of a `docker-compose.yml` may look like this:

```yml
version: '3'

services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/schaka/janitorr:stable
    user: 1000:1000 # Replace with your user who should own your application.yml file
    volumes:
      - /appdata/janitorr/config/application.yml:/workspace/application.yml
      - /share_media:/data
    healthcheck:
      test: "wget -T5 -qO- http://localhost:8081/health | grep UP || exit 1"
      start_period: 30s
      interval: 5s
      retries: 3
```

A native image is also published for every build. It keeps a much lower memory and CPU footprint and doesn't require longer runtimes to achieve optimal performance (JIT).
If you restart more often than once a week or have a very low powered server, this is now recommended.
That image is always tagged `:native-stable`. To get a specific version, use `:native-v1.x.x`.
While I do publish an arm64 version of this image, it is mostly untested.
The healthcheck also work slightly differently, see below:

```yml
version: '3'

services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/schaka/janitorr:native-stable
    user: 1000:1000 # Replace with your user who should own your application.yml file
    volumes:
      - /appdata/janitorr/config/application.yml:/workspace/application.yml
      - /share_media:/data
    environment:
      # Uses https://github.com/dmikusa/tiny-health-checker supplied by paketo buildpacks
      - THC_PATH=/health
      - THC_PORT=8081
    healthcheck:
      test: [ "CMD", "/workspace/health-check" ]
      start_period: 30s
      interval: 5s
      retries: 3
```

To get the latest build as found in the development branch, grab the following image: `ghcr.io/schaka/janitorr:develop`.
The development version of the native image is available as `ghcr.io/schaka/janitorr:native-develop`.


## JetBrains
Thank you to [<img src="logos/jetbrains.svg" alt="JetBrains" width="32"> JetBrains](http://www.jetbrains.com/) for providing us with free licenses to their great tools.

* [<img src="logos/idea.svg" alt="Idea" width="32"> IntelliJ Idea](https://www.jetbrains.com/idea/)
* [<img src="logos/webstorm.svg" alt="WebStorm" width="32"> WebStorm](http://www.jetbrains.com/webstorm/)
* [<img src="logos/rider.svg" alt="Rider" width="32"> Rider](http://www.jetbrains.com/rider/)
