package com.github.schaka.janitorr.servarr

interface ServarrProperties : RestClientProperties {
    val determineAgeBy: HistorySort?
    val importExclusions: Boolean
}