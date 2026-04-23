package com.github.schaka.janitorr.containers

import com.github.dockerjava.api.exception.NotFoundException
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.Network

const val NETWORK_NAME = "janitorr-local-dev"
private const val REMOVE_RETRY_INTERVAL_MS = 2000L
private const val REMOVE_TIMEOUT_MS = 60_000L

fun createLocalDevNetwork(): Network {
    val dockerClient = DockerClientFactory.instance().client()
    dockerClient.listNetworksCmd().exec()
        .filter { it.name == NETWORK_NAME }
        .forEach { network ->
            network.containers?.keys?.forEach { containerId ->
                runCatching {
                    dockerClient.disconnectFromNetworkCmd()
                        .withNetworkId(network.id)
                        .withContainerId(containerId)
                        .withForce(true)
                        .exec()
                }
            }

            val deadline = System.currentTimeMillis() + REMOVE_TIMEOUT_MS
            while (true) {
                val stillExists = dockerClient.listNetworksCmd().exec().any { it.id == network.id }
                if (!stillExists) break

                val result = runCatching { dockerClient.removeNetworkCmd(network.id).exec() }
                if (result.isSuccess) break

                val cause = result.exceptionOrNull()
                if (cause is NotFoundException) break

                if (System.currentTimeMillis() >= deadline) {
                    throw IllegalStateException("Timed out waiting for network '${NETWORK_NAME}' to be removed", cause)
                }

                Thread.sleep(REMOVE_RETRY_INTERVAL_MS)
            }
        }

    return Network.builder()
        .createNetworkCmdModifier { cmd -> cmd.withName(NETWORK_NAME) }
        .build()
}
