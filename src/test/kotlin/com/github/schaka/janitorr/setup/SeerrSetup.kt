package com.github.schaka.janitorr.setup

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class SeerrSetup(
    private val baseUrl: String,
    private val radarrApiKey: String,
    private val sonarrApiKey: String,
) {

    companion object {
        private val log = LoggerFactory.getLogger(SeerrSetup::class.java)
        private const val ADMIN_USER = "admin"
        private const val ADMIN_PASS = "adminadmin"
    }

    private val client = RestClient.create()

    fun setup(): String {
        val initialized = isInitialized()
        val sessionCookie = authenticate(initialized)
        if (sessionCookie.isBlank()) {
            log.warn("Could not authenticate with Seerr — setup may be incomplete")
            return ""
        }

        if (!initialized) {
            completeWizard(sessionCookie)
        }

        val apiKey = readApiKey(sessionCookie)
        if (apiKey.isBlank()) {
            log.warn("Could not retrieve Seerr API key — Seerr may require manual configuration")
            return ""
        }

        configureRadarr(apiKey)
        configureSonarr(apiKey)

        return apiKey
    }

    private fun isInitialized(): Boolean {
        return try {
            val body = client.get()
                .uri("$baseUrl/api/v1/settings/public")
                .retrieve()
                .body(String::class.java) ?: ""
            body.contains("\"initialized\":true") || body.contains("\"initialized\": true")
        } catch (e: Exception) {
            false
        }
    }

    private fun authenticate(initialized: Boolean): String {
        // Seerr MediaServerType enum: PLEX=1, JELLYFIN=2, EMBY=3, NOT_CONFIGURED=4
        // When Seerr already has Jellyfin configured (persisted state), sending hostname causes 500.
        // Only send hostname + serverType on fresh installs.
        val wizardBody = mapOf(
            "username" to ADMIN_USER,
            "password" to ADMIN_PASS,
            "email" to "test@mail.com",
            "urlBase" to "",
            "hostname" to "jellyfin",
            "port" to 8096,
            "useSsl" to false,
            "serverType" to 2,
        )
        val loginBody = mapOf(
            "username" to ADMIN_USER,
            "password" to ADMIN_PASS,
        )

        val body = if (initialized) loginBody else wizardBody

        repeat(5) { attempt ->
            try {
                val response = client.post()
                    .uri("$baseUrl/api/v1/auth/jellyfin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String::class.java)

                val cookie = response.headers["Set-Cookie"]?.firstOrNull() ?: ""
                if (response.statusCode.is2xxSuccessful && cookie.isNotBlank()) {
                    log.info("Seerr authenticated successfully (initialized={})", initialized)
                    return cookie
                }
            } catch (e: Exception) {
                log.warn("Seerr auth attempt {}/5 failed: {}", attempt + 1, e.message)
            }
            Thread.sleep(2_000)
        }
        return ""
    }

    private fun completeWizard(sessionCookie: String) {
        try {
            val librariesJson = client.get()
                .uri("$baseUrl/api/v1/settings/jellyfin/library?sync=true")
                .header("Cookie", sessionCookie)
                .retrieve()
                .body(String::class.java) ?: "[]"

            val libraryIds = parseLibraryIds(librariesJson)
            log.info("Found {} Jellyfin libraries to enable", libraryIds.size)

            libraryIds.forEach { libraryId ->
                try {
                    client.get()
                        .uri("$baseUrl/api/v1/settings/jellyfin/library?enable=$libraryId")
                        .header("Cookie", sessionCookie)
                        .retrieve()
                        .toBodilessEntity()
                    log.info("Enabled Jellyfin library {}", libraryId)
                } catch (e: Exception) {
                    log.warn("Failed to enable library {}: {}", libraryId, e.message)
                }
            }

            client.post()
                .uri("$baseUrl/api/v1/settings/initialize")
                .header("Cookie", sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve()
                .toBodilessEntity()
            log.info("Seerr wizard completed")
        } catch (e: Exception) {
            log.warn("Failed to complete Seerr wizard: {}", e.message)
        }
    }

    private fun readApiKey(sessionCookie: String): String {
        return try {
            val body = client.post()
                .uri("$baseUrl/api/v1/settings/main")
                .header("Cookie", sessionCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve()
                .body(String::class.java) ?: ""
            parseJsonString(body, "apiKey")
        } catch (e: Exception) {
            log.warn("Failed to read Seerr API key: {}", e.message)
            ""
        }
    }

    private fun configureRadarr(apiKey: String) {
        try {
            val existing = client.get()
                .uri("$baseUrl/api/v1/settings/radarr")
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(String::class.java) ?: ""
            if (existing.contains("\"hostname\":\"radarr\"")) {
                log.info("Radarr already configured in Seerr, skipping")
                return
            }
        } catch (e: Exception) {
            // proceed to configure
        }

        try {
            client.post()
                .uri("$baseUrl/api/v1/settings/radarr")
                .header("X-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "name" to "Radarr",
                        "hostname" to "radarr",
                        "port" to 7878,
                        "apiKey" to radarrApiKey,
                        "useSsl" to false,
                        "baseUrl" to "",
                        "activeProfileId" to 1,
                        "activeProfileName" to "Any",
                        "activeDirectory" to "/data/movies",
                        "is4k" to false,
                        "minimumAvailability" to "announced",
                        "isDefault" to true,
                        "externalUrl" to "",
                        "syncEnabled" to false,
                        "preventSearch" to false,
                    )
                )
                .retrieve()
                .toBodilessEntity()
            log.info("Radarr configured in Seerr")
        } catch (e: Exception) {
            log.warn("Failed to configure Radarr in Seerr: {}", e.message)
        }
    }

    private fun configureSonarr(apiKey: String) {
        try {
            val existing = client.get()
                .uri("$baseUrl/api/v1/settings/sonarr")
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(String::class.java) ?: ""
            if (existing.contains("\"hostname\":\"sonarr\"")) {
                log.info("Sonarr already configured in Seerr, skipping")
                return
            }
        } catch (e: Exception) {
            // proceed to configure
        }

        try {
            client.post()
                .uri("$baseUrl/api/v1/settings/sonarr")
                .header("X-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "name" to "Sonarr",
                        "hostname" to "sonarr",
                        "port" to 8989,
                        "apiKey" to sonarrApiKey,
                        "useSsl" to false,
                        "baseUrl" to "",
                        "activeProfileId" to 1,
                        "activeProfileName" to "Any",
                        "activeDirectory" to "/data/TV Shows",
                        "is4k" to false,
                        "enableSeasonFolders" to true,
                        "isDefault" to true,
                        "externalUrl" to "",
                        "syncEnabled" to false,
                        "preventSearch" to false,
                    )
                )
                .retrieve()
                .toBodilessEntity()
            log.info("Sonarr configured in Seerr")
        } catch (e: Exception) {
            log.warn("Failed to configure Sonarr in Seerr: {}", e.message)
        }
    }

    private fun parseJsonString(json: String, key: String): String {
        val marker = """"$key":""""
        val start = json.indexOf(marker)
        if (start == -1) return ""
        val valueStart = start + marker.length
        val end = json.indexOf('"', valueStart)
        return if (end == -1) "" else json.substring(valueStart, end)
    }

    private fun parseLibraryIds(json: String): List<String> {
        val ids = mutableListOf<String>()
        val marker = """"id":""""
        var pos = 0
        while (true) {
            val start = json.indexOf(marker, pos)
            if (start == -1) break
            val valueStart = start + marker.length
            val end = json.indexOf('"', valueStart)
            if (end == -1) break
            ids.add(json.substring(valueStart, end))
            pos = end + 1
        }
        return ids
    }
}
