package com.example.rygg.feature.library.data

import android.net.Uri
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.core.gpx.GpxAnalyzer
import com.example.rygg.core.gpx.GpxParser
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
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

    suspend fun importGpxFile(uri: Uri, discipline: Discipline): Outcome<Long> = outcomeCatching {
        val file = gpxStorage.saveFromUri(uri)
        val hash = gpxStorage.sha256(file)
        val originalName = gpxStorage.originalDisplayName(uri)
        val parsed = file.inputStream().use { gpxParser.parse(it) }
        val analysis = gpxAnalyzer.analyze(parsed.gpxDocument)
        val now = System.currentTimeMillis()
        val entry = GpxFileEntry(
            id = 0L,
            fileName = file.name,
            contentHash = hash,
            name = analysis.name.ifBlank { originalName?.substringBeforeLast(".") ?: file.name },
            description = analysis.description,
            color = null,
            discipline = discipline,
            isFavorite = false,
            distanceMeters = analysis.distanceMeters,
            ascentMeters = analysis.ascentMeters,
            descentMeters = analysis.descentMeters,
            elevationMeters = analysis.elevationMeters,
            pointCount = analysis.pointCount,
            routeCount = analysis.routeCount,
            waypointCount = analysis.waypointCount,
            hasTime = analysis.hasTime,
            startTimeMillis = analysis.startTimeMillis,
            movingTimeMillis = analysis.movingTimeMillis,
            totalTimeMillis = analysis.totalTimeMillis,
            minLat = analysis.minLat,
            minLon = analysis.minLon,
            maxLat = analysis.maxLat,
            maxLon = analysis.maxLon,
            pathPoints = analysis.simplifiedPath,
            folder = null,
            tags = emptyList(),
            importedAt = now,
            updatedAt = now,
            creator = analysis.creator,
            originalFileName = originalName
        )
        gpxFileEntryDao.insert(entry.toEntity())
    }

    suspend fun deleteGpxFile(entry: GpxFileEntry): Outcome<Unit> = outcomeCatching {
        gpxFileEntryDao.deleteById(entry.id)
        gpxStorage.deleteFile(entry.fileName)
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) =
        gpxFileEntryDao.setFavorite(id, favorite)
}
