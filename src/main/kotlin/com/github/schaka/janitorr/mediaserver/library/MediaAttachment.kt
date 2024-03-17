package com.github.schaka.janitorr.mediaserver.library

data class MediaAttachment(
        val Codec: String,
        val CodecTag: String,
        val Comment: String,
        val DeliveryUrl: String,
        val FileName: String,
        val Index: Int,
        val MimeType: String
)