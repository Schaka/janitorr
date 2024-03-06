package com.github.schaka.janitorr.mediaserver.emby

import feign.Param
import feign.RequestLine

interface EmbyUserClient {

    @RequestLine("DELETE /Items/{id}")
    fun deleteItemAndFiles(@Param("id") itemId: String)

}