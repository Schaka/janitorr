package com.github.schaka.janitorr.servarr.history

data class Revision(
    val isRepack: Boolean,
    val real: Int,
    val version: Int
)