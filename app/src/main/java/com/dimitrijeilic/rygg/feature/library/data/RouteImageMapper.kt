package com.dimitrijeilic.rygg.feature.library.data

import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageEntity
import com.dimitrijeilic.rygg.feature.library.domain.RouteImage

fun RouteImageEntity.toDomain(): RouteImage = RouteImage(
    id = id,
    entryId = entryId,
    position = position,
    width = width,
    height = height,
    isAvailable = fileDownloaded
)
