package com.github.schaka.janitorr.servarr

class TorrentNotInQueueException(override val message: String) : RuntimeException(message) {
}