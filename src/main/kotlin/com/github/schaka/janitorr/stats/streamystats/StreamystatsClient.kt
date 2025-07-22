package com.github.schaka.janitorr.stats.streamystats

import com.github.schaka.janitorr.stats.streamystats.requests.StreamystatsHistoryResponse
import feign.Param
import feign.RequestLine

interface StreamystatsClient {

    @RequestLine("GET /get-item-details/{itemId}")
    fun getRequests(@Param("itemId") itemId: String): StreamystatsHistoryResponse

    @RequestLine("GET /get-item-details/{itemId}")
    fun getRequestsDebug(@Param("itemId") itemId: String): Map<Object, Object>

}