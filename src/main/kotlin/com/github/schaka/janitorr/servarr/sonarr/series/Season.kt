package com.github.schaka.janitorr.servarr.sonarr.series

data class Season(
    var monitored: Boolean,
    val seasonNumber: Int,
    val statistics: Statistics
)