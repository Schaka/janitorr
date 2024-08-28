package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.emby.library.AddVirtualFolder
import com.github.schaka.janitorr.mediaserver.library.*
import com.github.schaka.janitorr.mediaserver.emby.library.AddMediaPathRequest
import com.github.schaka.janitorr.mediaserver.library.items.ItemPage
import feign.Param
import feign.RequestLine

interface EmbyMediaServerClient : MediaServerClient {

    @RequestLine("POST /Library/VirtualFolders?name={name}&collectionType={type}&refreshLibrary=false")
    fun createLibrary(@Param("name") name: String, @Param("type") collectionType: String, request: AddVirtualFolder)

    @RequestLine("POST /Library/VirtualFolders/Paths?refreshLibrary=false")
    fun addPathToLibrary(request: AddMediaPathRequest)

    @RequestLine("GET /Library/VirtualFolders/Query")
    fun listLibrariesPage(): ItemPage<VirtualFolderResponse>

    @RequestLine("POST /Environment/ValidatePath")
    fun validatePath(path: PathInfo)
}