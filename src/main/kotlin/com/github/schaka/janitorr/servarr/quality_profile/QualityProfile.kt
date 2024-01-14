package com.github.schaka.janitorr.servarr.quality_profile

data class QualityProfile(
        val id: Int,
        val name: String,
        val upgradeAllowed: Boolean,
        val items: List<Any>
)
