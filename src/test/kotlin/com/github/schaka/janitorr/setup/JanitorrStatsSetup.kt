package com.github.schaka.janitorr.setup

import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

class JanitorrStatsSetup(private val localRuntime: Path, private val jellyfinApiKey: String) {

    companion object {
        private val log = LoggerFactory.getLogger(JanitorrStatsSetup::class.java)
    }

    fun setup() {
        val configDir = localRuntime.resolve("janitorr-stats")
        Files.createDirectories(configDir)
        val configFile = configDir.resolve("application.yml")
        val config = """
            jellyfin:
              base-url: http://jellyfin:8096
              api-key: $jellyfinApiKey
              poll-interval: 60s

            quarkus:
              datasource:
                db-kind: sqlite
                jdbc:
                  url: jdbc:sqlite:/data/janitorr-stats.db
        """.trimIndent()
        Files.writeString(configFile, config)
        log.info("Wrote janitorr-stats config to {}", configFile)
    }
}
