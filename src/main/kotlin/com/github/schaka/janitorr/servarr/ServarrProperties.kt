package com.github.schaka.janitorr.servarr

interface ServarrProperties {
    val enabled: Boolean
    val url: String
    val apiKey: String
    val determineAgeBy: HistorySort?
}