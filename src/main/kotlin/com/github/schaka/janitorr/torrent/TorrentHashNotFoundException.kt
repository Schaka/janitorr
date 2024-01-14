package com.github.schaka.janitorr.torrent

class TorrentHashNotFoundException(override val message: String) : RuntimeException(message) {
}