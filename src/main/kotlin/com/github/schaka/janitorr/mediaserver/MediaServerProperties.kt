package com.github.schaka.janitorr.mediaserver

interface MediaServerProperties {
    val enabled: Boolean
    val url: String
    val apiKey: String
    val username: String
    val password: String
    val delete: Boolean
    val leavingSoonTv: String
    val leavingSoonMovies: String
    val leavingSoonType: LeavingSoonType
}
