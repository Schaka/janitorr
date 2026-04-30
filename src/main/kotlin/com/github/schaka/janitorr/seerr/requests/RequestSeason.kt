package com.github.schaka.janitorr.seerr.requests

data class RequestSeason(
        val createdAt: String,
        val id: Int,
        val seasonNumber: Int,
        val status: Int,
        val updatedAt: String
)