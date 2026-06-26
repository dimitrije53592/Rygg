package com.example.rygg.feature.home.data

import com.example.rygg.feature.home.data.local.ItemEntity
import com.example.rygg.feature.home.domain.Item

fun ItemEntity.toDomain(): Item = Item(id = id, title = title, createdAt = createdAt)

fun Item.toEntity(): ItemEntity = ItemEntity(id = id, title = title, createdAt = createdAt)
