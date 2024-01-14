package com.github.schaka.janitorr.servarr

interface ServarrService {

    fun getEntries(): List<LibraryItem>

    fun removeEntries(items: List<LibraryItem>)
}