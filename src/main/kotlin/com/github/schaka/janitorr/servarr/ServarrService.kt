package com.github.schaka.janitorr.servarr

import com.github.schaka.janitorr.servarr.history.HistoryResponse
import java.time.LocalDateTime

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

    fun historyByDate(): Comparator<HistoryResponse> {
        val comp = compareBy<HistoryResponse> { LocalDateTime.parse(it.date.substring(0, it.date.length - 1)) }
        return comp.reversed()
    }
}