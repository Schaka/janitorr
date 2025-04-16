package com.github.schaka.janitorr.stats.streamystats

import com.github.schaka.janitorr.stats.streamystats.requests.StreamystatsHistoryResponse
import feign.Param
import feign.RequestLine

/**
 * https://jellystat.server.com/swagger/
 */
interface StreamystatsClient {

    @RequestLine("GET /items/{itemId}")
    fun getRequests(@Param("itemId") itemId: String): StreamystatsHistoryResponse

}