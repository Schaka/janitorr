package com.github.schaka.janitorr.servarr.history

import com.github.schaka.janitorr.servarr.radarr.movie.QualityWrapper

open class HistoryResponse(
        val `data`: Data,
        val date: String,
        val eventType: String,
        val id: Int,
        val quality: QualityWrapper
)