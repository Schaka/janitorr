package com.github.schaka.janitorr.torrent.qbit

import com.github.schaka.janitorr.torrent.TorrentHashNotFoundException
import com.github.schaka.janitorr.torrent.TorrentInfo
import com.github.schaka.janitorr.torrent.TorrentService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@ConditionalOnProperty("clients.torrent.type", havingValue = "QBITTORRENT")
@Service
class QBittorrentService(
    @QBittorrent
    private var client: RestTemplate
) : TorrentService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun enrichTorrentInfo(info: TorrentInfo): TorrentInfo {
        val files = client.exchange(
            "/torrents/files?hash={hash}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<QbitFileResponse>>() {},
            info.hash.lowercase()
        )

        if (files.body.isNullOrEmpty()) {
            throw TorrentHashNotFoundException("Torrent (${info.torrentName}) (${info.hash}) not in torrent client or files cannot be read")
        }

        info.addFiles(files.body!!.map(QbitFileResponse::name))
        log.info(
            "Found torrent {} (hash: {}) at indexer {} with files ({}).",
            info.torrentName, info.hash, info.indexer, info.filenames
        )
        return info
    }

    override fun resumeTorrent(hash: String) {
        val map = LinkedMultiValueMap<String, Any>()
        map.add("hashes", hash)
        client.postForEntity(
            "/torrents/resume",
            map,
            String::class.java
        )
    }


}