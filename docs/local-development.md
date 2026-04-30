# Local Development

This guide covers how to run Janitorr locally with a full stack of real service containers, intended for developers who want to test behaviour end-to-end without manual Docker setup.

> **Note on code quality:** The local development infrastructure lives under `src/test/kotlin` and is intentionally held to a lower standard than the rest of the codebase. It is not shipped, changes frequently, and exists solely to make debugging easier. Expect rough edges.

## Overview

`LocalJanitorrStarter.kt` is the entry point. Running its `main` function starts a `LocalDevEnvironment`, which spins up a set of Testcontainers, configures them, then launches the full Spring Boot application with all client properties pointed at the local containers.

The following containers are started:

| Container | Port | Credentials |
|-----------|------|-------------|
| Radarr | random | admin / admin |
| Sonarr | random | admin / admin |
| Jellyfin | random | admin / adminadmin |
| Seerr | random | admin / adminadmin |
| janitorr-stats | random | n/a |

All containers share a Docker bridge network named `janitorr-local-dev` so they can reach each other by service name (e.g. `http://jellyfin:8096`). Exposed ports are mapped to random free ports on the host so nothing conflicts with an existing local setup.

## How startup works

Startup happens in `LocalDevEnvironment.start()` and follows a fixed sequence:

1. **Shared directories** are created under `local-runtime/` in the project root: `media/`, `downloads/`, and `downloads/incomplete/`.

2. **Media library preparation** runs in a background thread (`MediaLibrarySetup`). It downloads a small sample video file from the Blender Foundation and creates a realistic directory structure with symlinks for 10 movies and 5 TV shows under `local-runtime/media/`. The video file is cached in `local-runtime/media/cache/` and reused on subsequent runs.

3. **Radarr, Sonarr, Jellyfin, and Seerr** are started in parallel using `Startables.deepStart()`. janitorr-stats is not included here because it requires a valid Jellyfin API key at startup and cannot start with an empty config.

4. Each *arr reads its generated API key from the container's config file (`/config/config.xml`) by exec-ing into the container. This is retried a few times while the config is still being written.

5. **`RadarrSetup`** and **`SonarrSetup`** configure the running instances via their HTTP APIs: root folders, quality profiles, download clients, and naming conventions.

6. **`JellyfinSetup`** runs the initial setup wizard, creates an admin user, and generates a `janitorr-local` API key. The key is written to `local-runtime/jellyfin/api-key.txt`. On subsequent startups, if the file exists, the key is validated by calling `GET /Users/Me`. If it is still valid, the wizard and key creation steps are skipped entirely, which avoids accumulating stale API keys in Jellyfin.

7. **`JanitorrStatsSetup`** writes `local-runtime/janitorr-stats/application.yml` with the real Jellyfin API key, then janitorr-stats is started individually via `start()`. This ordering is required because janitorr-stats validates `jellyfin.api-key` at boot and will crash if it is missing or empty.

8. **`SeerrSetup`** configures Seerr: initial wizard, admin account, and Radarr/Sonarr integration.

9. All resolved ports and API keys are injected as `System.setProperty` calls so Spring Boot picks them up as if they were present in `application.yml`.

Once `start()` returns, the Spring Boot application starts normally. After it is ready, `logStartupInfo()` prints the URLs and credentials for all running services to the log.

## Running from IntelliJ

1. Open the `LocalJanitorrStarter.kt` file located at `src/test/kotlin/com/github/schaka/janitorr/LocalJanitorrStarter.kt`.
2. Click the green run icon next to the `fun main` declaration, or right-click the file and choose **Run 'LocalJanitorrStarterKt'**.
3. The first run will take several minutes. IntelliJ's Run panel will show container startup progress and log output from each setup step.
4. Once ready, the log will print something like:
   ```
   Started Radarr at http://localhost:54321  => Login via: admin/admin | API-Key: ...
   Started Sonarr at http://localhost:54322  => Login via: admin/admin | API-Key: ...
   Started Jellyfin at http://localhost:54323 => Login via: admin/adminadmin | API-Key: ...
   Started Seerr at http://localhost:54324 => Login via: admin/adminadmin | API-Key: ...
   Started Janitorr-Stats at http://localhost:54325
   ```
5. Janitorr itself is now running and connected to all services. You can interact with the UIs in a browser or attach a debugger in IntelliJ as usual.

## Persistent state

State that is expensive to recreate is persisted in `local-runtime/` so subsequent runs are faster:

- `local-runtime/media/cache/` - downloaded sample video files. Not deleted between runs.
- `local-runtime/jellyfin/api-key.txt` - the Jellyfin API key. Reused as long as it remains valid.
- `local-runtime/radarr/`, `local-runtime/sonarr/`, etc. - container config directories. Mounted into the containers each run.

`local-runtime/` is listed in `.gitignore` and is never committed.

## Network isolation

The Docker network is created fresh each time `LocalDevEnvironment.start()` is called. Each container class accepts an optional `network` parameter (defaulting to `null`) so that individual containers can also be used in isolation in focused integration tests, without needing the full stack.
