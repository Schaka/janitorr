package com.github.schaka.janitorr.seerr

import com.github.schaka.janitorr.seerr.paging.SeerrPage
import com.github.schaka.janitorr.seerr.requests.RequestResponse
import com.github.schaka.janitorr.seerr.servarr.ServarrSettings
import feign.Param
import feign.RequestLine

/**
 * https://api-docs.overseerr.dev/
 */
interface SeerrClient {

    @RequestLine("GET /request?take={pageSize}&skip={offset}")
    fun getRequests(@Param("pageSize") pageSize: Int, @Param("offset") offset: Int): SeerrPage<RequestResponse>

    @RequestLine("DELETE /request/{id}")
    fun deleteRequest(@Param("id") id: Int)

    @RequestLine("GET /settings/radarr")
    fun getRadarrServers(): List<ServarrSettings>

    @RequestLine("GET /settings/sonarr")
    fun getSonarrServers(): List<ServarrSettings>

}
