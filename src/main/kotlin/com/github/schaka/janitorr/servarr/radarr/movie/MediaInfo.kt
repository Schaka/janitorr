package com.github.schaka.janitorr.servarr.radarr.movie

data class MediaInfo(
        val audioBitrate: Int,
        val audioChannels: Int,
        val audioCodec: String,
        val audioLanguages: String,
        val audioStreamCount: Int,
        val id: Int,
        val resolution: String,
        val runTime: String,
        val scanType: String,
        val subtitles: String,
        val videoBitDepth: Int,
        val videoBitrate: Int,
        val videoCodec: String,
        val videoDynamicRange: String,
        val videoDynamicRangeType: String,
        val videoFps: Int
)