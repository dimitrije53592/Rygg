package com.dimitrijeilic.rygg.feature.map.data

import com.dimitrijeilic.rygg.core.gpx.GpxParser
import com.dimitrijeilic.rygg.core.gpx.decodeGeoPaths
import com.dimitrijeilic.rygg.core.gpx.encodeGeoPaths
import com.dimitrijeilic.rygg.core.gpx.model.GeoPoint
import com.dimitrijeilic.rygg.core.gpx.simplifyPath
import com.dimitrijeilic.rygg.core.gpx.trackPaths
import com.dimitrijeilic.rygg.feature.library.data.GpxStorage
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry
import com.dimitrijeilic.rygg.feature.map.data.local.RouteOverviewDao
import com.dimitrijeilic.rygg.feature.map.data.local.RouteOverviewEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteOverviewRepository @Inject constructor(
    private val dao: RouteOverviewDao,
    private val gpxStorage: GpxStorage,
    private val gpxParser: GpxParser
) {
    // Parses the .gpx only the first time (or after its content changes); later map opens read
    // the cached overview. Falls back to the thumbnail, uncached, while the file isn't local yet.
    suspend fun loadOverview(entry: GpxFileEntry): List<List<GeoPoint>> = withContext(Dispatchers.IO) {
        dao.get(entry.id)
            ?.takeIf { it.contentHash == entry.contentHash }
            ?.let { return@withContext decodeGeoPaths(it.paths) }

        val paths = runCatching {
            gpxStorage.resolve(entry.fileName)
                .inputStream()
                .use { gpxParser.parse(it).gpxDocument }
                .trackPaths()
                .filter { it.isNotEmpty() }
                .map { simplifyPath(it, OVERVIEW_TOLERANCE_METERS) }
        }.getOrNull()

        if (paths.isNullOrEmpty()) return@withContext listOfNotNull(entry.pathPoints.takeIf { it.isNotEmpty() })
        runCatching { dao.upsert(RouteOverviewEntity(entry.id, entry.contentHash, encodeGeoPaths(paths))) }
        paths
    }

    private companion object {
        const val OVERVIEW_TOLERANCE_METERS = 8.0
    }
}
