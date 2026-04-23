package com.github.schaka.janitorr.seerr.paging

import com.github.schaka.janitorr.seerr.requests.PageInfo

data class SeerrPage<T>(
        val pageInfo: PageInfo,
        val results: List<T>
)
