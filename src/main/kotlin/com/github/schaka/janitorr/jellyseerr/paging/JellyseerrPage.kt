package com.github.schaka.janitorr.jellyseerr.paging

import com.github.schaka.janitorr.jellyseerr.requests.PageInfo

data class JellyseerrPage<T>(
        val pageInfo: PageInfo,
        val results: List<T>
)