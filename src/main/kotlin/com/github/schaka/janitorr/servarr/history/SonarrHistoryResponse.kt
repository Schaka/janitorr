package com.github.schaka.janitorr.servarr.history

import com.github.schaka.janitorr.servarr.radarr.movie.QualityWrapper

class SonarrHistoryResponse(
        data: Data,
        date: String,
        eventType: String,
        id: Int,
        quality: QualityWrapper,
        val seriesId: Int,
        val episodeId: Int
) : HistoryResponse(data, date, eventType, id, quality)
