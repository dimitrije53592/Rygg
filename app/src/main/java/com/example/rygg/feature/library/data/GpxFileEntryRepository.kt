package com.example.rygg.feature.library.data

import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity
import com.example.rygg.feature.library.domain.GpxFileEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GpxFileEntryRepository @Inject constructor(
    private val gpxFileEntryDao: GpxFileEntryDao
) {
    fun observeGpxFileEntries(): Flow<List<GpxFileEntry>> =
        gpxFileEntryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

//    suspend fun addItem(title: String) =
//        gpxFileEntryDao.insert(GpxFileEntryEntity(title = title, createdAt = System.currentTimeMillis()))

    suspend fun deleteItem(id: Long) = gpxFileEntryDao.deleteById(id)
}
