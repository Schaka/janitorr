package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.jellyseerr.requests.RequestResponse
import com.github.schaka.janitorr.jellyseerr.servarr.ServarrSettings
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.radarr.RadarrProperties
import com.github.schaka.janitorr.servarr.sonarr.SonarrProperties
import org.apache.http.client.utils.URIBuilder
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriComponentsBuilder

class JellyseerrRestService(

    val jellyseerrClient: JellyseerrClient,
    val jellyseerrProperties: JellyseerrProperties,
    val sonarrProperties: SonarrProperties,
    val radarrProperties: RadarrProperties,
    val applicationProperties: ApplicationProperties,
    var sonarrServers: List<ServarrSettings> = listOf(),
    var radarrServers: List<ServarrSettings> = listOf()

) : JellyseerrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    init {
        if (jellyseerrProperties.enabled) {
            sonarrServers = jellyseerrClient.getSonarrServers()
            radarrServers = jellyseerrClient.getRadarrServers()
        }
    }

    override fun cleanupRequests(items: List<LibraryItem>) {
        val allRequests = getAllRequests()
        for (item: LibraryItem in items) {
            var requests: List<RequestResponse> =
                    // TV show
                    if (item.season != null) {
                        allRequests.filter { req -> mediaMatches(item, req) && req.seasons?.any { it.seasonNumber == item.season } ?: false }
                    }
                    // Movie
                    else {
                        allRequests.filter { req -> mediaMatches(item, req) }
                    }

            for (request: RequestResponse in requests) {
                if (!applicationProperties.dryRun) {
                    try {
                        log.info("Deleting request for {} | IMDB: {} - {}", request, item.filePath, item.imdbId, request)
                        jellyseerrClient.deleteRequest(request.id)
                    } catch(e: Exception) {
                        log.trace("Error deleting Jellyseerr request", e)
                    }
                } else {
                    log.info("Found request for {} | IMDB: {} - {}", request, item.filePath, item.imdbId, request)
                }
            }
        }
    }

    private fun mediaMatches(item: LibraryItem, candidate: RequestResponse): Boolean {

        if (!serverMatches(candidate)) {
            return false
        }

        // Check if the media type is the same before checking anything else
        if (!mediaTypeMatches(item, candidate)) {
            return false
        }

        // Match between Radarr ID or Sonarr ID and the ID Jellyseerr stores for Radarr/Sonarr
        // TODO: Maybe grab the Jellyfin ID here to make deletion in Jellyfin easier down the line?
        if (item.id == candidate.media.externalServiceId) {
            return true
        }

        // Fallback, match by meta data
        val imdbMatches = candidate.media.imdbId != null && (candidate.media.imdbId == item.imdbId)
        val tmdbMatches = candidate.media.tmdbId != null && (candidate.media.tmdbId == item.tmdbId)
        val tvdbMatches = candidate.media.tvdbId != null && (candidate.media.tvdbId == item.tvdbId)
        return imdbMatches || tmdbMatches || tvdbMatches
    }

    private fun serverMatches(candidate: RequestResponse): Boolean {
        // match media server first - some people use several Sonarr/Radarr installations
        val servarrrSettings = if (candidate.type == "tv") sonarrServers else radarrServers
        val servarrProperties = if (candidate.type == "tv") sonarrProperties else radarrProperties

        // find server this request is responsible for
        val serverSetting = servarrrSettings.firstOrNull { it.id == candidate.serverId }
        if (serverSetting == null) {
            log.debug("Found a Jellyseerr request [id: {}] not matching any known server: {}", candidate.id, candidate.media)
            return false
        }

        val targetServerUri = URIBuilder()
            .setScheme(if (serverSetting.useSsl) "https" else "http")
            .setHost(serverSetting.hostname)
            .setPort(serverSetting.port)
            .setPath("") // force empty path for reliable match
            .build()

        val settingServerUri = UriComponentsBuilder
            .fromHttpUrl(servarrProperties.url)
            .path("") // force empty path for reliable match
            .build().toUri()

        return targetServerUri.equals(settingServerUri)
    }

    private fun mediaTypeMatches(item: LibraryItem, candidate: RequestResponse): Boolean {

        // Found TV show, both request and potential media have seasons
        if (item.season != null && (candidate.type == "tv" || candidate.seasons?.isNotEmpty() == true)) {
            return true
        }

        // No seasons? Found a movie
        if (item.season == null && (candidate.type == "movie" || candidate.seasons.isNullOrEmpty())) {
            return true
        }

        return false
    }

    private fun getAllRequests(): List<RequestResponse> {
        val allRequests = mutableListOf<RequestResponse>()
        val pageSize = 1000
        var page = 0
        var pages: Int

        do {
            val pageResult = jellyseerrClient.getRequests(pageSize, page * pageSize)
            pages = pageResult.pageInfo.pages
            allRequests.addAll(pageResult.results)
            page++
        } while (page < pages)

        return allRequests
    }

}
