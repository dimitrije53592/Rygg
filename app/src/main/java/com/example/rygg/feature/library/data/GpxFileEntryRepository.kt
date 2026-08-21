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
import com.example.rygg.feature.sync.data.RouteSyncManager
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
    private val gpxWriter: GpxWriter,
    private val routeSyncManager: RouteSyncManager
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
        gpxFileEntryDao.insert(entry.toEntity()).also { routeSyncManager.onRouteAdded(it) }
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
        gpxFileEntryDao.insert(toSave.toEntity()).also { routeSyncManager.onRouteAdded(it) }
    }

    // Delegates to the sync manager: local delete plus cloud removal when the route is owned.
    suspend fun deleteGpxFile(entry: GpxFileEntry): Outcome<Unit> = outcomeCatching {
        routeSyncManager.deleteRoute(entry)
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) {
        gpxFileEntryDao.setFavorite(id, favorite)
        routeSyncManager.onRouteChanged(id)
    }

    // Rename an entry: update the display name and rename the on-disk .gpx to match.
    suspend fun renameGpxFile(entry: GpxFileEntry, newName: String): Outcome<Unit> = outcomeCatching {
        val finalName = newName.trim().ifBlank { entry.name }
        val newFileName = gpxStorage.rename(entry.fileName, finalName)
        gpxFileEntryDao.updateNameAndFile(
            id = entry.id,
            name = finalName,
            fileName = newFileName,
            updatedAt = System.currentTimeMillis()
        )
        routeSyncManager.onRouteChanged(entry.id)
    }

    // Shareable content Uri for an entry's .gpx file (see GpxStorage.shareUri).
    fun gpxShareUri(entry: GpxFileEntry): Uri = gpxStorage.shareUri(entry.fileName)

    // Track paths and waypoints from the GPX file. When the file isn't downloaded yet (a route
    // pulled from another device), fall back to the synced simplified geometry (entry.pathPoints)
    // so the map/following still render instead of collapsing to RouteProgress.EMPTY.
    suspend fun loadRouteContent(entry: GpxFileEntry): RouteFileContent = withContext(Dispatchers.IO) {
        val fromFile = runCatching {
            val document = gpxStorage.resolve(entry.fileName)
                .inputStream()
                .use { gpxParser.parse(it).gpxDocument }
            RouteFileContent(
                paths = document.trackPaths(),
                waypoints = document.waypoints.map { Waypoint(it.lat, it.lon, it.name.orEmpty()) }
            )
        }.getOrNull()

        when {
            fromFile != null && fromFile.paths.any { it.isNotEmpty() } -> fromFile
            entry.pathPoints.isNotEmpty() ->
                RouteFileContent(paths = listOf(entry.pathPoints), waypoints = emptyList())
            else -> fromFile ?: RouteFileContent(emptyList(), emptyList())
        }
    }

    // Fetch a synced route's .gpx locally so a screen can upgrade from the simplified fallback.
    suspend fun ensureRouteFileDownloaded(id: Long) = routeSyncManager.ensureFileDownloaded(id)

    // Back up an owned route's .gpx to the cloud, healing "ghosts" whose file never uploaded.
    suspend fun ensureRouteFileUploaded(id: Long) = routeSyncManager.ensureFileUploaded(id)

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
