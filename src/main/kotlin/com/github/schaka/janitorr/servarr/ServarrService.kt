package com.github.schaka.janitorr.servarr

interface ServarrService {

    fun getEntries(): List<LibraryItem>

    fun removeEntries(items: List<LibraryItem>)

    /**
     * Sort by oldest file. If upgrades are allowed, sort by most recently grabbed files.
     */
    fun byDate(upgradesAllowed: Boolean): Comparator<LibraryItem> {
        val comp = compareBy<LibraryItem> { it.importedDate }
        return if (upgradesAllowed) comp.reversed() else comp
    }
}