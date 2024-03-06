package com.github.schaka.janitorr.mediaserver.jellyfin

import feign.Param
import feign.RequestLine

interface JellyfinUserClient {

    @RequestLine("DELETE /Items/{id}")
    fun deleteItemAndFiles(@Param("id") itemId: String)

}