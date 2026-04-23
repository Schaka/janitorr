package com.github.schaka.janitorr.containers

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class JellyfinContainer(localRuntime: Path, network: Network? = null) :
    GenericContainer<JellyfinContainer>("jellyfin/jellyfin:latest") {

    init {
        Files.createDirectories(localRuntime.resolve("jellyfin/config"))
        Files.createDirectories(localRuntime.resolve("jellyfin/cache"))
        withExposedPorts(8096)
        withEnv("JELLYFIN_LOG_DIR", "/config/log")
        withFileSystemBind(
            localRuntime.resolve("jellyfin/config").toString(),
            "/config",
            BindMode.READ_WRITE
        )
        withFileSystemBind(
            localRuntime.resolve("jellyfin/cache").toString(),
            "/cache",
            BindMode.READ_WRITE
        )
        withFileSystemBind(
            localRuntime.resolve("media").toString(),
            "/data",
            BindMode.READ_ONLY
        )
        val home = Path.of(System.getProperty("user.home"))
        val uid = Files.getAttribute(home, "unix:uid") as Int
        val gid = Files.getAttribute(home, "unix:gid") as Int
        withCreateContainerCmdModifier { it.withUser("$uid:$gid") }
        waitingFor(
            Wait.forHttp("/health")
                .forPort(8096)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(3))
        )
        network?.let { withNetwork(it).withNetworkAliases("jellyfin") }
    }
}
