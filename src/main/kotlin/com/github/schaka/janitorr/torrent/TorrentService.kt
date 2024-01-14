package com.github.schaka.janitorr.torrent

interface TorrentService {

    /**
     * Checks the torrent's contents and returns filenames
     */
    @Throws(TorrentHashNotFoundException::class)
    fun enrichTorrentInfo(info: TorrentInfo): TorrentInfo

    fun resumeTorrent(hash: String)
}