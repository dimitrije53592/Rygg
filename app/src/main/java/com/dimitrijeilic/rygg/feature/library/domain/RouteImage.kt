package com.dimitrijeilic.rygg.feature.library.domain

import android.net.Uri

data class RouteImage(
    val id: String,
    val entryId: Long,
    val uri: Uri,
    val position: Int,
    val width: Int,
    val height: Int,
    // False while a photo synced from another device is still downloading.
    val isAvailable: Boolean
)

const val MAX_IMAGES_PER_ROUTE = 20
