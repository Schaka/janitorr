package com.github.schaka.janitorr.mediaserver

import feign.Param
import feign.RequestLine

interface MediaServerUserClient {

    @RequestLine("DELETE /Items/{id}")
    fun deleteItemAndFiles(@Param("id") itemId: String)

}