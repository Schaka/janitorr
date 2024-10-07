package com.github.schaka.janitorr.servarr

interface RestClientProperties {
    val enabled: Boolean
    val url: String
    val apiKey: String
}