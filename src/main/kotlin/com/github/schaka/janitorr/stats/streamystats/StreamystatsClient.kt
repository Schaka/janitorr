package com.github.schaka.janitorr.stats.streamystats

import com.github.schaka.janitorr.stats.streamystats.requests.StreamystatsHistoryResponse
import feign.Param
import feign.RequestLine

interface StreamystatsClient {

    @RequestLine("GET /get-item-details/{itemId}")
    fun getRequests(@Param("itemId") itemId: String): StreamystatsHistoryResponse

}