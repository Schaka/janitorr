package com.github.schaka.janitorr.torrent

class TorrentInfo(
        val hash: String,
        val downloadClientType: String,
        val downloadClient: String,
        val indexer: String,
        val torrentName: String
) {

    val filenames: MutableList<String> = mutableListOf()

    fun addFiles(files: List<String>) {
        this.filenames.addAll(files)
    }

}