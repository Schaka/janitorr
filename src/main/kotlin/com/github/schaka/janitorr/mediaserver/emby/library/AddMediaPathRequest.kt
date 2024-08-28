package com.github.schaka.janitorr.mediaserver.emby.library

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
data class AddMediaPathRequest(
        val Id: String,
        val Guid: String,
        val Name: String,
        val PathInfo: PathInfo,
)