package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.jellyseerr.paging.JellyseerrPage
import com.github.schaka.janitorr.jellyseerr.requests.RequestResponse
import com.github.schaka.janitorr.jellyseerr.servarr.ServarrSettings
import feign.Param
import feign.RequestLine

/**
 * https://api-docs.overseerr.dev/
 */
interface JellyseerrClient {

    @RequestLine("GET /request?take={pageSize}&skip={offset}")
    fun getRequests(@Param("pageSize") pageSize: Int, @Param("offset") offset: Int): JellyseerrPage<RequestResponse>

    @RequestLine("DELETE /request/{id}")
    fun deleteRequest(@Param("id") id: Int)

    @RequestLine("GET /settings/radarr")
    fun getRadarrServers(): List<ServarrSettings>

    @RequestLine("GET /settings/sonarr")
    fun getSonarrServers(): List<ServarrSettings>


}