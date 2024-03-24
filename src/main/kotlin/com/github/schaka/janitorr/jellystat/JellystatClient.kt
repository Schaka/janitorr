package com.github.schaka.janitorr.jellystat

import com.github.schaka.janitorr.jellystat.requests.ItemRequest
import com.github.schaka.janitorr.jellystat.requests.WatchHistoryResponse
import feign.RequestLine

/**
 * https://jellystat.server.com/swagger/
 */
interface JellystatClient {

    @RequestLine("POST /getItemHistory")
    fun getRequests(request: ItemRequest): List<WatchHistoryResponse>

}