package com.github.schaka.janitorr.jellyseerr

import com.github.schaka.janitorr.jellyfin.library.VirtualFolderResponse
import com.github.schaka.janitorr.jellyseerr.paging.JellyseerrPage
import com.github.schaka.janitorr.jellyseerr.requests.RequestResponse
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


}