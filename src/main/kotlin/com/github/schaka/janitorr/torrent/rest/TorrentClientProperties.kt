package com.github.schaka.janitorr.torrent.rest

import com.github.schaka.janitorr.torrent.TorrentClientType
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.torrent")
data class TorrentClientProperties(
        val type: TorrentClientType,
        val name: String,
        val autoResume: Boolean = true,
        val url: String,
        val username: String,
        val password: String
)