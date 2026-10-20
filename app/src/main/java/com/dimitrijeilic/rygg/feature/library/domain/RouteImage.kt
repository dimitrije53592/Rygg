package com.dimitrijeilic.rygg.feature.library.domain

data class RouteImage(
    val id: String,
    val entryId: Long,
    val position: Int,
    val width: Int,
    val height: Int,
    // False while a photo synced from another device is still downloading.
    val isAvailable: Boolean
)

const val MAX_IMAGES_PER_ROUTE = 20
