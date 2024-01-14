package com.github.schaka.janitorr.torrent

import com.github.schaka.janitorr.torrent.rest.TorrentClientProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TorrentManager(
        private val torrentService: TorrentService?,
        private val torrentClientProperties: TorrentClientProperties,
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }


}