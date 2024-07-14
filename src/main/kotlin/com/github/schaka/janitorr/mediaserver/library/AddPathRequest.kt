package com.github.schaka.janitorr.mediaserver.library

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
data class AddPathRequest(
        val Name: String,
        val PathInfo: PathInfo,

        // Emby only - needs cleaner solution where the two are split properly
        val Id: String? = null
)