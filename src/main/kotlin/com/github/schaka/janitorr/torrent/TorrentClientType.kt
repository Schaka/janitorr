package com.github.schaka.janitorr.torrent

enum class TorrentClientType(
    val servarrName: String
) {
    QBITTORRENT("qBittorrent"),
    TRANSMISSION("Transmission")
}