package com.example.rygg.feature.home.data

import com.example.rygg.feature.home.data.local.ItemDao
import com.example.rygg.feature.home.data.local.ItemEntity
import com.example.rygg.feature.home.domain.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepository @Inject constructor(
    private val itemDao: ItemDao
) {
    fun observeItems(): Flow<List<Item>> =
        itemDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun addItem(title: String) =
        itemDao.insert(ItemEntity(title = title, createdAt = System.currentTimeMillis()))

    suspend fun deleteItem(id: Long) = itemDao.deleteById(id)
}
