package com.github.schaka.janitorr.containers

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class RadarrContainer(localRuntime: Path, network: Network? = null) : GenericContainer<RadarrContainer>("lscr.io/linuxserver/radarr:latest") {

    init {
        Files.createDirectories(localRuntime.resolve("radarr"))
        withExposedPorts(7878)
        withEnv("PUID", "1000")
        withEnv("PGID", "1000")
        withEnv("TZ", "UTC")
        withFileSystemBind(localRuntime.resolve("radarr").toString(), "/config", BindMode.READ_WRITE)
        withFileSystemBind(localRuntime.resolve("media").toString(), "/data", BindMode.READ_WRITE)
        withFileSystemBind(localRuntime.resolve("downloads").toString(), "/downloads", BindMode.READ_WRITE)
        waitingFor(Wait.forHttp("/ping").forPort(7878).withStartupTimeout(Duration.ofMinutes(5)))
        network?.let { withNetwork(it).withNetworkAliases("radarr") }
    }

    fun readApiKey(): String {
        repeat(10) {
            try {
                val result = execInContainer("cat", "/config/config.xml")
                if (result.exitCode == 0 && "<ApiKey>" in result.stdout) {
                    return result.stdout.substringAfter("<ApiKey>").substringBefore("</ApiKey>").trim()
                }
            } catch (e: Exception) {
                // Config not yet written
            }
            Thread.sleep(2_000)
        }
        throw IllegalStateException("Could not read Radarr API key from /config/config.xml after waiting 20 seconds")
    }
}
