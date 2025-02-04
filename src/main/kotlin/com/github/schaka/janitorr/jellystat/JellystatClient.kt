package com.github.schaka.janitorr.jellystat

import com.github.schaka.janitorr.jellystat.requests.ItemRequest
import com.github.schaka.janitorr.jellystat.requests.JellystatPage
import com.github.schaka.janitorr.jellystat.requests.WatchHistoryResponse
import feign.RequestLine

/**
 * https://jellystat.server.com/swagger/
 */
interface JellystatClient {

    @RequestLine("POST /getItemHistory?page=1&size=100000")
    fun getRequests(request: ItemRequest): JellystatPage<WatchHistoryResponse>

}