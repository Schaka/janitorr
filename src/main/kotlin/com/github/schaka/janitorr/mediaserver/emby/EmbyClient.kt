package com.github.schaka.janitorr.mediaserver.emby

import com.github.schaka.janitorr.mediaserver.jellyfin.api.User
import com.github.schaka.janitorr.mediaserver.jellyfin.library.*
import com.github.schaka.janitorr.mediaserver.jellyfin.library.items.ItemPage
import com.github.schaka.janitorr.mediaserver.jellyfin.library.items.MediaFolderItem
import feign.Param
import feign.RequestLine

interface EmbyClient {

    @RequestLine("GET /Users")
    fun listUsers(): List<User>

    @RequestLine("GET /Library/VirtualFolders")
    fun listLibraries(): List<VirtualFolderResponse>

    @RequestLine("POST /Library/VirtualFolders?name={name}&collectionType={type}&paths={paths}&refreshLibrary=false")
    fun createLibrary(@Param("name") name: String, @Param("type") collectionType: String, request: AddLibraryRequest, @Param("paths") paths: List<String> = listOf())

    @RequestLine("POST /Collections?name={name}&ids&parentId={parentId}&isLocked=true")
    fun createCollection(@Param("name") name: String, @Param("parentId") parentId: String? = null): CollectionResponse

    @RequestLine("POST /Library/VirtualFolders/Paths?refreshLibrary={refresh}")
    fun addPathToLibrary(request: AddPathRequest, @Param("refresh") refresh: Boolean = true)

    @RequestLine("DELETE /Library/VirtualFolders/Paths?name={name}&path={path}&refreshLibrary={refresh}")
    fun removePathFromLibrary(@Param("name") name: String, @Param("path") path: String, @Param("refresh") refresh: Boolean = true)

    @RequestLine("POST /Collections/{id}/Items?ids={itemIds}")
    fun addItemToCollection(@Param("id") id: String, itemIds: List<String>)

    @RequestLine("GET /Library/MediaFolders")
    fun getAllItems(): ItemPage<MediaFolderItem>

    @RequestLine("GET /Items?limit=10000&includeItemTypes=Series&parentId={parentId}&fields=Path,ProviderIds")
    fun getAllTvShows(@Param("parentId") parentId: String): ItemPage<LibraryContent>

    @RequestLine("GET /Items?limit=10000&includeItemTypes=Movies&parentId={parentId}&fields=Path,ProviderIds")
    fun getAllMovies(@Param("parentId") parentId: String): ItemPage<LibraryContent>


    @RequestLine("GET /Shows/{tvshow}/Seasons?fields=Path,ProviderIds")
    fun getAllSeasons(@Param("tvshow") showId: String): ItemPage<LibraryContent>

    @RequestLine("GET /Movies/{movieId}?fields=Path,ProviderIds")
    fun getMovie(@Param("movieId") movieId: String): ItemPage<LibraryContent>
}