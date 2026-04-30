package com.github.schaka.janitorr.containers

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import java.nio.file.Files
import java.nio.file.Path

class JanitorrStatsContainer(localRuntime: Path, network: Network? = null) :
    GenericContainer<JanitorrStatsContainer>("ghcr.io/schaka/janitorr-stats:stable-sqlite") {

    init {
        Files.createDirectories(localRuntime.resolve("janitorr-stats/data"))
        withExposedPorts(8080)
        withEnv("TZ", "UTC")
        withFileSystemBind(
            localRuntime.resolve("janitorr-stats/application.yml").toString(),
            "/work/config/application.yml",
            BindMode.READ_ONLY
        )
        withFileSystemBind(
            localRuntime.resolve("janitorr-stats/data").toString(),
            "/data",
            BindMode.READ_WRITE
        )
        val home = Path.of(System.getProperty("user.home"))
        val uid = Files.getAttribute(home, "unix:uid") as Int
        val gid = Files.getAttribute(home, "unix:gid") as Int
        withCreateContainerCmdModifier { it.withUser("$uid:$gid") }
        waitingFor(Wait.forListeningPort())
        network?.let { withNetwork(it).withNetworkAliases("janitorr-stats") }
    }
}
