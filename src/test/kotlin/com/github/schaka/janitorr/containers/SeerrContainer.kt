package com.github.schaka.janitorr.containers

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class SeerrContainer(localRuntime: Path, network: Network? = null) :
    GenericContainer<SeerrContainer>("ghcr.io/seerr-team/seerr:latest") {

    init {
        Files.createDirectories(localRuntime.resolve("seerr"))
        withExposedPorts(5055)
        withEnv("LOG_LEVEL", "info")
        withFileSystemBind(localRuntime.resolve("seerr").toString(), "/app/config", BindMode.READ_WRITE)
        val home = Path.of(System.getProperty("user.home"))
        val uid = Files.getAttribute(home, "unix:uid") as Int
        val gid = Files.getAttribute(home, "unix:gid") as Int
        withCreateContainerCmdModifier { it.withUser("$uid:$gid") }
        waitingFor(
            Wait.forHttp("/api/v1/settings/public")
                .forPort(5055)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(3))
        )
        network?.let { withNetwork(it).withNetworkAliases("seerr") }
    }
}
