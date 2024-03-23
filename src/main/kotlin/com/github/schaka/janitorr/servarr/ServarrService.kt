package com.github.schaka.janitorr.servarr

import jakarta.annotation.PostConstruct

interface ServarrService {

    fun getEntries(): List<LibraryItem>

    fun removeEntries(items: List<LibraryItem>)

    /**
     * Sort by oldest file. If upgrades are allowed, sort by most recently grabbed files.
     */
    fun byDate(upgradesAllowed: Boolean): Comparator<LibraryItem> {
        val comp = compareBy<LibraryItem> { it.date }
        return if (upgradesAllowed) comp.reversed() else comp
    }
}