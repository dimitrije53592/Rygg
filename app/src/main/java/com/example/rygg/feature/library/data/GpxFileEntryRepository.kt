package com.example.rygg.feature.library.data

import android.net.Uri
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.core.gpx.GpxAnalyzer
import com.example.rygg.core.gpx.GpxParser
import com.example.rygg.core.gpx.GpxWriter
import com.example.rygg.core.gpx.haversineMeters
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.RouteFileContent
import com.example.rygg.core.gpx.model.Waypoint
import com.example.rygg.core.gpx.trackPaths
import com.example.rygg.core.gpx.trackSegments
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.example.rygg.feature.library.domain.EntrySource
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
    private val gpxAnalyzer: GpxAnalyzer,
    private val gpxWriter: GpxWriter
) {
    fun observeGpxFileEntries(): Flow<List<GpxFileEntry>> =
        gpxFileEntryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeGpxFileEntry(id: Long): Flow<GpxFileEntry?> =
        gpxFileEntryDao.observeById(id).map { entity -> entity?.toDomain() }

    // Copy + parse a picked file into an unsaved entry (id = 0). The file is stored, but
    // nothing is inserted until persistGpxFile — see the import-preview flow.
    suspend fun stageGpxFile(uri: Uri, discipline: Discipline): Outcome<GpxFileEntry> = outcomeCatching {
        val file = gpxStorage.saveFromUri(uri)
        val hash = gpxStorage.sha256(file)
        val originalName = gpxStorage.originalDisplayName(uri)
        val parsed = file.inputStream().use { gpxParser.parse(it) }
        val analysis = gpxAnalyzer.analyze(parsed.gpxDocument)
        val now = System.currentTimeMillis()
        GpxFileEntry(
            id = 0L,
            fileName = file.name,
            contentHash = hash,
            name = analysis.name.ifBlank { originalName?.substringBeforeLast(".") ?: file.name },
            description = analysis.description,
            color = null,
            discipline = discipline,
            source = EntrySource.IMPORTED,
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
    }

    suspend fun persistGpxFile(entry: GpxFileEntry): Outcome<Long> = outcomeCatching {
        gpxFileEntryDao.insert(entry.toEntity())
    }

    suspend fun discardStagedFile(entry: GpxFileEntry) {
        gpxStorage.deleteFile(entry.fileName)
    }

    // Delete stored .gpx files that no library row references — staged files orphaned when the
    // process died or the task was swiped during a preview. Safe as a one-shot on library load:
    // by then any legitimately-staged file is already persisted or was explicitly discarded.
    suspend fun reconcileOrphanedFiles() {
        val referenced = gpxFileEntryDao.getAllFileNames().toSet()
        gpxStorage.listedStoredFiles().forEach { stored ->
            if (stored.fileName !in referenced) gpxStorage.deleteFile(stored.fileName)
        }
    }

    // Serialize a recorded document to a staged .gpx file and build an unsaved RECORDED entry.
    suspend fun stageRecordedTrack(document: GpxDocument, discipline: Discipline): Outcome<GpxFileEntry> = outcomeCatching {
        val file = gpxStorage.saveText(gpxWriter.write(document))
        val hash = gpxStorage.sha256(file)
        val analysis = gpxAnalyzer.analyze(document)
        val now = System.currentTimeMillis()
        GpxFileEntry(
            id = 0L,
            fileName = file.name,
            contentHash = hash,
            name = analysis.name,
            description = analysis.description,
            color = null,
            discipline = discipline,
            source = EntrySource.RECORDED,
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
            originalFileName = file.name
        )
    }

    // Persist a recorded entry, renaming the staged file to the user-chosen name.
    suspend fun persistRecordedTrack(entry: GpxFileEntry, name: String): Outcome<Long> = outcomeCatching {
        val finalName = name.ifBlank { entry.name }.ifBlank { DEFAULT_RECORDING_NAME }
        val newFileName = gpxStorage.rename(entry.fileName, finalName)
        val toSave = entry.copy(name = finalName, fileName = newFileName, originalFileName = newFileName)
        gpxFileEntryDao.insert(toSave.toEntity())
    }

    suspend fun deleteGpxFile(entry: GpxFileEntry): Outcome<Unit> = outcomeCatching {
        gpxFileEntryDao.deleteById(entry.id)
        gpxStorage.deleteFile(entry.fileName)
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) =
        gpxFileEntryDao.setFavorite(id, favorite)

    // Shareable content Uri for an entry's .gpx file (see GpxStorage.shareUri).
    fun gpxShareUri(entry: GpxFileEntry): Uri = gpxStorage.shareUri(entry.fileName)

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

private const val DEFAULT_RECORDING_NAME = "Recording"
