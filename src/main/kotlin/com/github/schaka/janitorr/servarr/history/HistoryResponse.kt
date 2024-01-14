package com.github.schaka.janitorr.servarr.history

data class HistoryResponse(
        val customFormatScore: Int,
        val customFormats: List<CustomFormat>,
        val `data`: Data,
        val date: String,
        val downloadId: String?,
        val episodeId: Int,
        val eventType: String,
        val id: Int,
        val languages: List<Language>,
        val quality: Quality,
        val qualityCutoffNotMet: Boolean,
        val seriesId: Int,
        val sourceTitle: String
)