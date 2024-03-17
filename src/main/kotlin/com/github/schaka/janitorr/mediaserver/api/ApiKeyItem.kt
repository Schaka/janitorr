package com.github.schaka.janitorr.mediaserver.api

data class ApiKeyItem(
        val AccessToken: String,
        val AppName: String,
        val AppVersion: String,
        val DateCreated: String,
        val DateLastActivity: String?,
        val DateRevoked: String?,
        val DeviceId: String,
        val DeviceName: String,
        val Id: Int,
        val IsActive: Boolean,
        val UserId: String,
        val UserName: String?
)