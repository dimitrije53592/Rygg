package com.dimitrijeilic.rygg.feature.library.data

import android.net.Uri
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageEntity
import com.dimitrijeilic.rygg.feature.library.domain.RouteImage

fun RouteImageEntity.toDomain(uri: Uri): RouteImage = RouteImage(
    id = id,
    entryId = entryId,
    uri = uri,
    position = position,
    width = width,
    height = height,
    isAvailable = fileDownloaded
)
