package com.example.rygg.feature.library.data

import android.net.Uri
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.core.gpx.GpxAnalyzer
import com.example.rygg.core.gpx.GpxParser
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity
import com.example.rygg.feature.library.domain.GpxFileEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GpxFileEntryRepository @Inject constructor(
    private val gpxFileEntryDao: GpxFileEntryDao,
    private val gpxStorage: GpxStorage,
    private val gpxParser: GpxParser,
    private val gpxAnalyzer: GpxAnalyzer
) {
    fun observeGpxFileEntries(): Flow<List<GpxFileEntry>> =
        gpxFileEntryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun importGpxFile(uri: Uri): Outcome<Long> = outcomeCatching {
        val file = gpxStorage.saveFromUri(uri)
        val parsed = file.inputStream().use { gpxParser.parse(it)}
        val entry = gpxAnalyzer.analyze(parsed.gpxDocument)
        gpxFileEntryDao.insert(entry.toEntity())
    }

    suspend fun deleteItem(id: Long) = gpxFileEntryDao.deleteById(id)
}
