package com.github.schaka.janitorr.jellyfin.library

data class UserData(
    val IsFavorite: Boolean?,
    val ItemId: String?,
    val Key: String?,
    val LastPlayedDate: String?,
    val Likes: Boolean?,
    val PlayCount: Int?,
    val PlaybackPositionTicks: Int?,
    val Played: Boolean?,
    val PlayedPercentage: Int?,
    val Rating: Int?,
    val UnplayedItemCount: Int?
)