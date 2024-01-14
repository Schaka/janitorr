package com.github.schaka.janitorr.jellyfin.library

data class ImageBlurHashes(
    val Art: Map<String, String>?,
    val Backdrop: Map<String, String>?,
    val Banner: Map<String, String>?,
    val Box: Map<String, String>?,
    val BoxRear: Map<String, String>?,
    val Chapter: Map<String, String>?,
    val Disc: Map<String, String>?,
    val Logo: Map<String, String>?,
    val Menu: Map<String, String>?,
    val Primary: Map<String, String>?,
    val Profile: Map<String, String>?,
    val Screenshot: Map<String, String>?,
    val Thumb: Map<String, String>?
)