package com.github.schaka.janitorr.setup

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException

class SonarrSetup(baseUrl: String, apiKey: String) : ServarrSetup(baseUrl, apiKey) {

    private val log = LoggerFactory.getLogger(SonarrSetup::class.java)

    fun setup() {
        val rootFolderId = setupRootFolder("/data/TV Shows", "tv")
        setupAuth()
        importMedia(rootFolderId)
    }

    override fun lookupAndImport(folderName: String, folderPath: String) {
        val lookupResponse = client.get()
            .uri("$baseUrl/api/v3/series/lookup?term={term}", folderName)
            .header("X-Api-Key", apiKey)
            .retrieve()
            .body(String::class.java) ?: "[]"

        @Suppress("UNCHECKED_CAST")
        val results = objectMapper.readValue(lookupResponse, List::class.java) as List<Map<String, Any?>>
        if (results.isEmpty()) {
            log.warn("No metadata found for: $folderName")
            return
        }

        val series = results[0].toMutableMap()
        val tvdbId = (series["tvdbId"] as? Number)?.toInt()

        val seriesId = existingSeriesId(tvdbId) ?: addSeries(series, folderPath, folderName) ?: return
        triggerRescan(seriesId)
    }

    private fun existingSeriesId(tvdbId: Int?): Int? {
        if (tvdbId == null) return null
        return try {
            val response = client.get()
                .uri("$baseUrl/api/v3/series")
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(String::class.java) ?: "[]"

            @Suppress("UNCHECKED_CAST")
            val allSeries = objectMapper.readValue(response, List::class.java) as List<Map<String, Any?>>
            val match = allSeries.firstOrNull { (it["tvdbId"] as? Number)?.toInt() == tvdbId }
            (match?.get("id") as? Number)?.toInt()
        } catch (e: Exception) {
            null
        }
    }

    private fun addSeries(series: MutableMap<String, Any?>, folderPath: String, folderName: String): Int? {
        series["path"] = folderPath
        series["rootFolderPath"] = "/data/TV Shows"
        series["monitored"] = true
        series["qualityProfileId"] = 1
        series["languageProfileId"] = 1
        series["seriesType"] = "standard"
        series["seasonFolder"] = true
        series["addOptions"] = mapOf(
            "ignoreEpisodesWithFiles" to false,
            "ignoreEpisodesWithoutFiles" to false,
            "searchForMissingEpisodes" to false,
            "monitor" to "all"
        )

        return try {
            val response = client.post()
                .uri("$baseUrl/api/v3/series")
                .header("X-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(series)
                .retrieve()
                .body(String::class.java) ?: return null

            @Suppress("UNCHECKED_CAST")
            val added = objectMapper.readValue(response, Map::class.java) as Map<String, Any?>
            val id = (added["id"] as? Number)?.toInt()
            log.info("Added series: $folderName (ID: $id)")
            id
        } catch (e: HttpClientErrorException) {
            log.warn("Failed to add series '$folderName': ${e.message}")
            null
        }
    }

    private fun triggerRescan(seriesId: Int) {
        try {
            client.post()
                .uri("$baseUrl/api/v3/command")
                .header("X-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("name" to "RescanSeries", "seriesId" to seriesId))
                .retrieve()
                .toBodilessEntity()
            log.info("Triggered rescan for series ID: $seriesId")
        } catch (e: Exception) {
            log.warn("Failed to trigger rescan for series $seriesId: ${e.message}")
        }
    }
}
