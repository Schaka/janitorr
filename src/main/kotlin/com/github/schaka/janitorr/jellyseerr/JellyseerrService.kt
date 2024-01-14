package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.jellyfin.library.LibraryContent
import com.github.schaka.janitorr.jellyseerr.requests.RequestResponse
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JellyseerrService(

        val jellyseerrClient: JellyseerrClient,
        val applicationProperties: ApplicationProperties

) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }


    fun cleanupRequests(items: List<LibraryItem>) {
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
                    jellyseerrClient.deleteRequest(request.id)
                } else {
                    log.info("Found request: {}", request)
                }
            }
        }
    }

    private fun mediaMatches(item: LibraryItem, candidate: RequestResponse): Boolean {
        val imdbMatches = candidate.media.imdbId != null && (candidate.media.imdbId == item.imdbId)
        val tmdbMatches = candidate.media.tmdbId != null && mediaTypeMatches(item, candidate) && (candidate.media.tmdbId == item.tmdbId)
        val tvdbMatches = candidate.media.tvdbId != null && mediaTypeMatches(item, candidate) && (candidate.media.tvdbId == item.tvdbId)
        return imdbMatches || tmdbMatches || tvdbMatches
    }

    private fun mediaTypeMatches(item: LibraryItem, candidate: RequestResponse): Boolean {
        // Found TV show, both request and potential media have seasons
        if (item.season != null && candidate.seasons?.isNotEmpty() == true) {
            return true
        }

        // No seasons? Found a movie
        if (item.season == null && candidate.seasons.isNullOrEmpty()) {
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