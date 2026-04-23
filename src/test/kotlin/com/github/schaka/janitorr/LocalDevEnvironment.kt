package com.github.schaka.janitorr

import com.github.schaka.janitorr.containers.*
import com.github.schaka.janitorr.setup.*
import org.slf4j.LoggerFactory
import org.testcontainers.lifecycle.Startables
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class LocalDevEnvironment {

    private val log = LoggerFactory.getLogger(LocalDevEnvironment::class.java)
    private val projectRoot: Path = Path.of(System.getProperty("user.dir"))
    private val localRuntime: Path = projectRoot.resolve("local-runtime")

    private var radarrPort: Int = 0
    private var sonarrPort: Int = 0
    private var jellyfinPort: Int = 0
    private var seerrPort: Int = 0
    private var janitorrStatsPort: Int = 0
    private var radarrApiKey: String = ""
    private var sonarrApiKey: String = ""
    private var jellyfinApiKey: String = ""
    private var seerrApiKey: String = ""

    fun start() {
        createSharedDirectories()

        val mediaFuture = CompletableFuture.runAsync {
            log.info("Preparing media library...")
            MediaLibrarySetup(localRuntime.resolve("media")).prepare()
            log.info("Media library ready")
        }

        val network = createLocalDevNetwork()
        val radarr = RadarrContainer(localRuntime, network)
        val sonarr = SonarrContainer(localRuntime, network)
        val jellyfin = JellyfinContainer(localRuntime, network)
        val seerr = SeerrContainer(localRuntime, network)

        Startables.deepStart(radarr, sonarr, jellyfin, seerr).join()
        mediaFuture.join()

        radarrPort = radarr.getMappedPort(7878)
        sonarrPort = sonarr.getMappedPort(8989)
        jellyfinPort = jellyfin.getMappedPort(8096)
        seerrPort = seerr.getMappedPort(5055)

        radarrApiKey = radarr.readApiKey()
        RadarrSetup("http://localhost:$radarrPort", radarrApiKey).setup()

        sonarrApiKey = sonarr.readApiKey()
        SonarrSetup("http://localhost:$sonarrPort", sonarrApiKey).setup()

        jellyfinApiKey = JellyfinSetup("http://localhost:$jellyfinPort", localRuntime.resolve("jellyfin/api-key.txt")).setup()

        JanitorrStatsSetup(localRuntime, jellyfinApiKey).setup()
        val janitorrStats = JanitorrStatsContainer(localRuntime, network)
        janitorrStats.start()
        janitorrStatsPort = janitorrStats.getMappedPort(8080)

        seerrApiKey = SeerrSetup(
            baseUrl = "http://localhost:$seerrPort",
            radarrApiKey = radarrApiKey,
            sonarrApiKey = sonarrApiKey,
        ).setup()

        System.setProperty("clients.radarr.enabled", "true")
        System.setProperty("clients.radarr.url", "http://localhost:$radarrPort")
        System.setProperty("clients.radarr.api-key", radarrApiKey)

        System.setProperty("clients.sonarr.enabled", "true")
        System.setProperty("clients.sonarr.url", "http://localhost:$sonarrPort")
        System.setProperty("clients.sonarr.api-key", sonarrApiKey)

        System.setProperty("clients.jellyfin.enabled", "true")
        System.setProperty("clients.jellyfin.url", "http://localhost:$jellyfinPort")
        System.setProperty("clients.jellyfin.api-key", jellyfinApiKey)
        System.setProperty("clients.jellyfin.username", "admin")
        System.setProperty("clients.jellyfin.password", "adminadmin")

        if (seerrApiKey.isNotBlank()) {
            System.setProperty("clients.jellyseerr.enabled", "true")
            System.setProperty("clients.jellyseerr.url", "http://localhost:$seerrPort")
            System.setProperty("clients.jellyseerr.api-key", seerrApiKey)
        } else {
            log.warn("Seerr setup did not complete — Seerr integration will be disabled")
        }

        System.setProperty("clients.janitorr-stats.enabled", "true")
        System.setProperty("clients.janitorr-stats.url", "http://localhost:$janitorrStatsPort")

        System.setProperty("application.media-deletion.enabled", "true")
        System.setProperty("application.tag-based-deletion.enabled", "false")
        System.setProperty("application.episode-deletion.enabled", "false")
        System.setProperty("application.dry-run", "false")
    }

    fun logStartupInfo() {
        log.info("Started Radarr at http://localhost:$radarrPort => Login via: admin/admin | API-Key: $radarrApiKey")
        log.info("Started Sonarr at http://localhost:$sonarrPort => Login via: admin/admin | API-Key: $sonarrApiKey")
        log.info("Started Jellyfin at http://localhost:$jellyfinPort => Login via: admin/adminadmin | API-Key: $jellyfinApiKey")
        log.info("Started Seerr at http://localhost:$seerrPort => Login via: admin/adminadmin | API-Key: $seerrApiKey")
        log.info("Started Janitorr-Stats at http://localhost:$janitorrStatsPort")
    }

    private fun createSharedDirectories() {
        listOf(
            localRuntime.resolve("media"),
            localRuntime.resolve("downloads"),
            localRuntime.resolve("downloads/incomplete"),
        ).forEach(Files::createDirectories)
    }

}
