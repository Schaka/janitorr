package com.github.schaka.janitorr.mediaserver.emby.library

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.github.schaka.janitorr.mediaserver.library.PathInfo

@JsonInclude(NON_NULL)
data class AddMediaPathRequest(
        val Id: String,
        val Name: String,
        val Path: String,
        val PathInfo: List<PathInfo>,
)