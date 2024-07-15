package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.emby.library.AddVirtualFolder
import com.github.schaka.janitorr.mediaserver.library.*
import feign.Param
import feign.RequestLine

interface EmbyMediaServerClient : MediaServerClient {

    @RequestLine("POST /Library/VirtualFolders")
    fun createLibrary(request: AddVirtualFolder)
}