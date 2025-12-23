package com.github.schaka.janitorr.extensions

fun List<String>.removeSubsequence(sub: List<String>): List<String> {

    if (sub.isEmpty()) {
        return this
    }

    val filteredList = mutableListOf<String>()
    var i = 0
    while (i < size) {
        if (i + sub.size <= size && sub.indices.all { this[i + it] == sub[it] }) {
            i += sub.size
        } else {
            filteredList += this[i]
            i++
        }
    }
    return filteredList
}