package com.github.schaka.janitorr.setup

class RadarrSetup(baseUrl: String, apiKey: String) : ServarrSetup(baseUrl, apiKey) {

    fun setup() {
        val rootFolderId = setupRootFolder("/data/movies", "movies")
        setupAuth()
        importMedia(rootFolderId)
    }
}
