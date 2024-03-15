package com.github.schaka.janitorr.mediaserver.library.items


data class ItemPage<T>(
        val Items: List<T>,
        val StartIndex: Int,
        val TotalRecordCount: Int
)