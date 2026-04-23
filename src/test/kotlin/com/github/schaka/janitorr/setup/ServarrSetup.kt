package com.github.schaka.janitorr.setup

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import tools.jackson.databind.ObjectMapper

open class ServarrSetup(protected val baseUrl: String, protected val apiKey: String) {

    private val log = LoggerFactory.getLogger(ServarrSetup::class.java)
    protected val client = RestClient.create()
    protected val objectMapper = ObjectMapper()

    fun setupRootFolder(path: String, folderName: String): Int {
        return try {
            val existingFolders = client.get()
                .uri("$baseUrl/api/v3/rootfolder")
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(String::class.java) ?: "[]"

            if (existingFolders.trim() == "[]") {
                val response = client.post()
                    .uri("$baseUrl/api/v3/rootfolder")
                    .header("X-Api-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        mapOf(
                            "name" to folderName,
                            "path" to path,
                            "defaultMetadataProfileId" to 1,
                            "defaultQualityProfileId" to 1,
                            "defaultMonitorOption" to "all",
                            "defaultNewItemMonitorOption" to "all",
                        )
                    )
                    .retrieve()
                    .body(String::class.java) ?: "{}"

                val rootFolderId = extractId(response)
                log.info("Root folder '$folderName' created at $path (ID: $rootFolderId)")
                rootFolderId
            } else {
                log.info("Root folder already exists, skipping setup")
                extractFirstId(existingFolders)
            }
        } catch (e: Exception) {
            log.warn("Failed to setup root folder: ${e.message}")
            1
        }
    }

    fun setupAuth() {
        try {
            val hostConfigRaw = client.get()
                .uri("$baseUrl/api/v3/config/host")
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(String::class.java) ?: return

            if (!hostConfigRaw.contains(Regex("\"authenticationMethod\"\\s*:\\s*\"none\""))) {
                log.info("Authentication already configured, skipping setup")
                return
            }

            val updatedConfig = hostConfigRaw
                .replace(Regex("\"authenticationMethod\"\\s*:\\s*\"none\""), "\"authenticationMethod\":\"forms\"")
                .replace(Regex("\"authenticationRequired\"\\s*:\\s*\"disabled\""), "\"authenticationRequired\":\"enabled\"")
                .replace(Regex("\"username\"\\s*:\\s*\"\""), "\"username\":\"admin\"")
                .replace(Regex("\"password\"\\s*:\\s*\"\""), "\"password\":\"admin\"")
                .replace(Regex("\"passwordConfirmation\"\\s*:\\s*\"\""), "\"passwordConfirmation\":\"admin\"")

            client.put()
                .uri("$baseUrl/api/v3/config/host")
                .header("X-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedConfig)
                .retrieve()
                .toBodilessEntity()

            log.info("Authentication setup complete (admin/admin)")
        } catch (e: Exception) {
            log.warn("Auth setup incomplete: ${e.message} — configure admin/admin manually at $baseUrl")
        }
    }

    fun importMedia(rootFolderId: Int) {
        try {
            Thread.sleep(2_000)
            val rootFolderResponse = client.get()
                .uri("$baseUrl/api/v3/rootfolder/{id}?timeout=false", rootFolderId)
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(String::class.java) ?: return

            val folders = parseUnmappedFolders(rootFolderResponse)
            if (folders.isEmpty()) {
                log.info("No unmapped folders detected")
                return
            }

            log.info("Found {} unmapped folders, importing...", folders.size)
            for (folder in folders) {
                val folderName = folder["name"] as? String ?: continue
                val folderPath = folder["path"] as? String ?: continue
                try {
                    lookupAndImport(folderName, folderPath)
                } catch (e: Exception) {
                    log.warn("Error importing '$folderName': ${e.message}")
                }
            }
        } catch (e: Exception) {
            log.warn("Error during media import: ${e.message}")
        }
    }

    protected open fun lookupAndImport(folderName: String, folderPath: String) {
        val lookupResponse = client.get()
            .uri("$baseUrl/api/v3/movie/lookup?term={term}", folderName)
            .header("X-Api-Key", apiKey)
            .retrieve()
            .body(String::class.java) ?: "[]"

        @Suppress("UNCHECKED_CAST")
        val results = objectMapper.readValue(lookupResponse, List::class.java) as List<Map<String, Any?>>
        if (results.isEmpty()) {
            log.warn("No metadata found for: $folderName")
            return
        }

        val movie = results[0].toMutableMap()
        movie["path"] = folderPath
        movie["monitored"] = true
        movie["qualityProfileId"] = 1
        movie["minimumAvailability"] = "announced"
        movie["addOptions"] = mapOf("monitor" to "movieOnly", "searchForMovie" to false)

        client.post()
            .uri("$baseUrl/api/v3/movie/import")
            .header("X-Api-Key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(listOf(movie))
            .retrieve()
            .toBodilessEntity()
        log.info("Imported: $folderName")
    }

    private fun parseUnmappedFolders(json: String): List<Map<String, Any>> {
        return try {
            val root = objectMapper.readValue(json, Map::class.java)
            @Suppress("UNCHECKED_CAST")
            (root["unmappedFolders"] as? List<Map<String, Any>>) ?: emptyList()
        } catch (e: Exception) {
            log.warn("Failed to parse unmapped folders: ${e.message}")
            emptyList()
        }
    }

    private fun extractId(json: String): Int {
        return try {
            val root = objectMapper.readValue(json, Map::class.java)
            (root["id"] as? Number)?.toInt() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    private fun extractFirstId(json: String): Int {
        return try {
            @Suppress("UNCHECKED_CAST")
            val root = objectMapper.readValue(json, List::class.java) as List<Map<String, Any>>
            (root.firstOrNull()?.get("id") as? Number)?.toInt() ?: 1
        } catch (e: Exception) {
            1
        }
    }
}
