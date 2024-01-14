package com.github.schaka.janitorr.torrent.transmission

data class TransmissionTorrentResponse(val torrents: List<TransmissionTorrentInfo>)
data class TransmissionTorrentInfo(val files: List<TransmissionFileInfo>)
data class TransmissionFileInfo(val name: String)