package com.github.schaka.janitorr.seerr.servarr

data class ServarrSettings(
    val id: Int,
    val useSsl: Boolean,
    val hostname: String,
    val port: Int
)
