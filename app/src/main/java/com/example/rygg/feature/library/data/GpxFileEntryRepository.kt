package com.example.rygg.feature.library.data

import android.net.Uri
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.core.gpx.GpxAnalyzer
import com.example.rygg.core.gpx.GpxParser
import com.example.rygg.core.gpx.haversineMeters
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.gpx.model.RouteFileContent
import com.example.rygg.core.gpx.model.Waypoint
import com.example.rygg.core.gpx.trackPaths
import com.example.rygg.core.gpx.trackSegments
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.example.rygg.feature.library.domain.GpxFileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GpxFileEntryRepository @Inject constructor(
    private val gpxFileEntryDao: GpxFileEntryDao,
    private val gpxStorage: GpxStorage,
    private val gpxParser: GpxParser,
    private val gpxAnalyzer: GpxAnalyzer
) {
    fun observeGpxFileEntries(): Flow<List<GpxFileEntry>> =
        gpxFileEntryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeGpxFileEntry(id: Long): Flow<GpxFileEntry?> =
        gpxFileEntryDao.observeById(id).map { entity -> entity?.toDomain() }

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

    // Track paths and waypoints from a single parse of the GPX file.
    suspend fun loadRouteContent(entry: GpxFileEntry): RouteFileContent = withContext(Dispatchers.IO) {
        runCatching {
            val document = gpxStorage.resolve(entry.fileName)
                .inputStream()
                .use { gpxParser.parse(it).gpxDocument }
            RouteFileContent(
                paths = document.trackPaths(),
                waypoints = document.waypoints.map { Waypoint(it.lat, it.lon, it.name.orEmpty()) }
            )
        }.getOrDefault(RouteFileContent(emptyList(), emptyList()))
    }

    // Elevation-over-distance series for the details profile chart; empty when the file has no `ele` data.
    suspend fun loadElevationProfile(entry: GpxFileEntry): List<ElevationSample> = withContext(Dispatchers.IO) {
        runCatching {
            val segments = gpxStorage.resolve(entry.fileName)
                .inputStream()
                .use { gpxParser.parse(it).gpxDocument }
                .trackSegments()

            val samples = mutableListOf<ElevationSample>()
            var cumulativeMeters = 0.0
            segments.forEach { points ->
                points.zipWithNext().forEach { (previous, current) ->
                    cumulativeMeters += haversineMeters(previous.lat, previous.lon, current.lat, current.lon)
                    val elevation = current.ele ?: return@forEach
                    if (samples.isEmpty()) {
                        val firstElevation = previous.ele ?: elevation
                        samples += ElevationSample(distanceMeters = 0.0, elevationMeters = firstElevation)
                    }
                    samples += ElevationSample(distanceMeters = cumulativeMeters, elevationMeters = elevation)
                }
            }
            if (samples.size < 2) emptyList() else samples
        }.getOrDefault(emptyList())
    }
}
