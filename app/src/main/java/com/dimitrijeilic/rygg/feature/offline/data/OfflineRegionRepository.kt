package com.dimitrijeilic.rygg.feature.offline.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.outcomeCatching
import com.dimitrijeilic.rygg.feature.map.domain.MapStyleSource
import com.dimitrijeilic.rygg.feature.offline.domain.DownloadEstimate
import com.dimitrijeilic.rygg.feature.offline.domain.OfflineRegion
import com.dimitrijeilic.rygg.feature.offline.domain.RegionBounds
import com.dimitrijeilic.rygg.feature.offline.domain.RegionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.maplibre.android.offline.OfflineRegion as MapLibreRegion

@Singleton
class OfflineRegionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mapStyleSource: MapStyleSource,
    private val deviceStorage: DeviceStorage
) {
    private val tracked = MutableStateFlow<Map<Long, TrackedRegion>?>(null)
    private val loadMutex = Mutex()
    private var isThrottled = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val activationTimeouts = mutableMapOf<Long, Runnable>()

    private val offlineManager by lazy {
        MapLibre.getInstance(context)
        OfflineManager.getInstance(context)
    }

    fun observeRegions(): Flow<List<OfflineRegion>> =
        tracked
            .onStart { ensureLoaded() }
            .filterNotNull()
            .map { regions -> regions.values.map { it.toDomain() }.sortedByDescending { it.createdAt } }
            .distinctUntilChanged()

    suspend fun freeBytes(): Long = withContext(Dispatchers.IO) { deviceStorage.freeBytes() }

    suspend fun region(id: Long): OfflineRegion? = observeRegions().first().firstOrNull { it.id == id }

    suspend fun startDownload(name: String, bounds: RegionBounds, sourceRouteId: Long?): Outcome<Long> =
        outcomeCatching {
            withContext(Dispatchers.Main) {
                ensureLoaded()
                val now = System.currentTimeMillis()
                val metadata = OfflineMetadata(
                    name = name,
                    bounds = bounds,
                    createdAt = now,
                    updatedAt = now,
                    sourceRouteId = sourceRouteId
                )
                check(!hasActiveDownload()) { "Another offline download is running" }
                val region = createRegion(bounds, metadata)
                tracked.update { it.orEmpty() + (region.id to TrackedRegion(region, metadata, region.awaitStatus())) }
                observe(region)
                activate(region)
                region.id
            }
        }

    suspend fun resume(id: Long): Outcome<Unit> = withRegion(id) { region ->
        check(!hasActiveDownload()) { "Another offline download is running" }
        activate(region)
    }

    suspend fun pause(id: Long): Outcome<Unit> = withRegion(id) { region ->
        region.setDownloadState(MapLibreRegion.STATE_INACTIVE)
    }

    suspend fun update(id: Long): Outcome<Unit> = withRegion(id) { region ->
        check(!hasActiveDownload()) { "Another offline download is running" }
        region.awaitInvalidate()
        updateMetadata(id) { it.copy(updatedAt = System.currentTimeMillis()) }
        activate(region)
    }

    suspend fun rename(id: Long, name: String): Outcome<Unit> = withRegion(id) {
        updateMetadata(id) { it.copy(name = name.trim()) }
    }

    suspend fun delete(id: Long): Outcome<Long> = outcomeCatching {
        withContext(Dispatchers.Main) {
            ensureLoaded()
            val entry = tracked.value?.get(id) ?: error("Unknown offline region $id")
            entry.region.setDownloadState(MapLibreRegion.STATE_INACTIVE)
            entry.region.setObserver(null)
            entry.region.awaitDelete()
            tracked.update { it?.minus(id) }
            applyThrottle()
            entry.status?.completedResourceSize ?: 0L
        }
    }

    private suspend fun <T> withRegion(id: Long, block: suspend (MapLibreRegion) -> T): Outcome<T> =
        outcomeCatching {
            withContext(Dispatchers.Main) {
                ensureLoaded()
                val region = tracked.value?.get(id)?.region ?: error("Unknown offline region $id")
                block(region)
            }
        }

    private suspend fun ensureLoaded() {
        if (tracked.value != null) return
        loadMutex.withLock {
            if (tracked.value != null) return
            withContext(Dispatchers.Main) {
                val regions = offlineManager.awaitRegions().associate { region ->
                    val metadata = OfflineMetadata.decode(region.metadata) ?: region.fallbackMetadata()
                    region.id to TrackedRegion(region, metadata, region.awaitStatus())
                }
                tracked.value = regions
                regions.values.forEach { observe(it.region) }
            }
        }
    }

    private fun activate(region: MapLibreRegion) {
        tracked.update { regions ->
            val entry = regions?.get(region.id) ?: return@update regions
            regions + (region.id to entry.copy(failed = false, isActivating = true))
        }
        region.setDownloadState(MapLibreRegion.STATE_ACTIVE)
        applyThrottle()
        activationTimeouts.remove(region.id)?.let(mainHandler::removeCallbacks)
        val timeout = Runnable { endActivation(region.id) }
        activationTimeouts[region.id] = timeout
        mainHandler.postDelayed(timeout, ACTIVATION_TIMEOUT_MILLIS)
    }

    private fun endActivation(id: Long) {
        activationTimeouts.remove(id)
        val entry = tracked.value?.get(id)?.takeIf { it.isActivating } ?: return
        tracked.update { regions -> regions?.plus(id to entry.copy(isActivating = false)) }
        entry.status?.let { onStatus(id, it) }
    }

    private fun hasActiveDownload(): Boolean = tracked.value.orEmpty().values.any { it.isDownloading }

    private fun observe(region: MapLibreRegion) {
        region.setDeliverInactiveMessages(true)
        region.setObserver(
            object : MapLibreRegion.OfflineRegionObserver {
                override fun onStatusChanged(status: OfflineRegionStatus) = onStatus(region.id, status)

                override fun onError(error: OfflineRegionError) = onDownloadError(region, error)

                override fun mapboxTileCountLimitExceeded(limit: Long) {
                    region.setDownloadState(MapLibreRegion.STATE_INACTIVE)
                    markFailed(region.id)
                }
            }
        )
    }

    private fun onStatus(id: Long, status: OfflineRegionStatus) {
        val entry = tracked.value?.get(id) ?: return
        val isActive = status.downloadState == MapLibreRegion.STATE_ACTIVE
        val restarted = status.completedResourceCount < (entry.status?.completedResourceCount ?: 0L)
        val isActivating = entry.isActivating && isActive && status.isComplete && !restarted
        val progressed = status.completedResourceCount > (entry.status?.completedResourceCount ?: 0L)
        val failed = entry.failed && !progressed && !status.isComplete
        tracked.update { regions ->
            regions?.plus(id to entry.copy(status = status, failed = failed, isActivating = isActivating))
        }
        if (status.isComplete && isActive && !isActivating) {
            entry.region.setDownloadState(MapLibreRegion.STATE_INACTIVE)
        }
        applyThrottle()
    }

    private fun onDownloadError(region: MapLibreRegion, error: OfflineRegionError) {
        val entry = tracked.value?.get(region.id) ?: return
        val nothingDownloaded = (entry.status?.completedResourceCount ?: 0L) == 0L
        if (error.reason == OfflineRegionError.REASON_NOT_FOUND && nothingDownloaded) {
            region.setDownloadState(MapLibreRegion.STATE_INACTIVE)
            markFailed(region.id)
        }
    }

    private fun markFailed(id: Long) {
        tracked.update { regions ->
            val entry = regions?.get(id) ?: return@update regions
            regions + (id to entry.copy(failed = true))
        }
    }

    private suspend fun updateMetadata(id: Long, transform: (OfflineMetadata) -> OfflineMetadata) {
        val entry = tracked.value?.get(id) ?: error("Unknown offline region $id")
        val metadata = transform(entry.metadata)
        entry.region.awaitUpdateMetadata(metadata.encode())
        tracked.update { regions -> regions?.get(id)?.let { regions + (id to it.copy(metadata = metadata)) } ?: regions }
    }

    private fun applyThrottle() {
        val downloading = tracked.value.orEmpty().values.any { it.isDownloading }
        if (downloading == isThrottled) return
        HttpRequestUtil.setOkHttpClient(if (downloading) throttledClient else null)
        isThrottled = downloading
    }

    private suspend fun createRegion(bounds: RegionBounds, metadata: OfflineMetadata): MapLibreRegion {
        val definition = OfflineTilePyramidRegionDefinition(
            mapStyleSource.offlinePackStyleUrl(),
            LatLngBounds.from(bounds.north, bounds.east, bounds.south, bounds.west),
            DownloadEstimate.MIN_ZOOM.toDouble(),
            DownloadEstimate.MAX_ZOOM.toDouble(),
            context.resources.displayMetrics.density
        )
        return suspendCancellableCoroutine { continuation ->
            offlineManager.createOfflineRegion(
                definition,
                metadata.encode(),
                object : OfflineManager.CreateOfflineRegionCallback {
                    override fun onCreate(offlineRegion: MapLibreRegion) = continuation.resume(offlineRegion)

                    override fun onError(error: String) = continuation.resumeWithException(IllegalStateException(error))
                }
            )
        }
    }

    private fun MapLibreRegion.fallbackMetadata(): OfflineMetadata {
        val box = (definition as? OfflineTilePyramidRegionDefinition)?.bounds
        return OfflineMetadata(
            name = "",
            bounds = RegionBounds(
                south = box?.latitudeSouth ?: 0.0,
                west = box?.longitudeWest ?: 0.0,
                north = box?.latitudeNorth ?: 0.0,
                east = box?.longitudeEast ?: 0.0
            ),
            createdAt = 0L,
            updatedAt = 0L
        )
    }

    private data class TrackedRegion(
        val region: MapLibreRegion,
        val metadata: OfflineMetadata,
        val status: OfflineRegionStatus?,
        val failed: Boolean = false,
        val isActivating: Boolean = false
    ) {
        val isDownloading: Boolean
            get() = isActivating || (status?.downloadState == MapLibreRegion.STATE_ACTIVE && !status.isComplete)

        fun toDomain() = OfflineRegion(
            id = region.id,
            name = metadata.name,
            bounds = metadata.bounds,
            createdAt = metadata.createdAt,
            updatedAt = metadata.updatedAt,
            sourceRouteId = metadata.sourceRouteId,
            sizeBytes = status?.completedResourceSize ?: 0L,
            progress = status?.progress ?: 0f,
            status = when {
                isDownloading -> RegionStatus.Downloading
                failed -> RegionStatus.Failed
                status?.isComplete == true -> RegionStatus.Complete
                else -> RegionStatus.Paused
            }
        )
    }

    private companion object {
        const val MAX_REQUESTS_PER_HOST = 4
        const val ACTIVATION_TIMEOUT_MILLIS = 15_000L

        val throttledClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .dispatcher(Dispatcher().apply { maxRequestsPerHost = MAX_REQUESTS_PER_HOST })
                .build()
        }
    }
}

private val OfflineRegionStatus.progress: Float
    get() = if (requiredResourceCount > 0) {
        (completedResourceCount.toFloat() / requiredResourceCount).coerceIn(0f, 1f)
    } else {
        0f
    }

private suspend fun OfflineManager.awaitRegions(): List<MapLibreRegion> =
    suspendCancellableCoroutine { continuation ->
        listOfflineRegions(
            object : OfflineManager.ListOfflineRegionsCallback {
                override fun onList(offlineRegions: Array<MapLibreRegion>?) =
                    continuation.resume(offlineRegions.orEmpty().toList())

                override fun onError(error: String) = continuation.resumeWithException(IllegalStateException(error))
            }
        )
    }

private suspend fun MapLibreRegion.awaitStatus(): OfflineRegionStatus? =
    suspendCancellableCoroutine { continuation ->
        getStatus(
            object : MapLibreRegion.OfflineRegionStatusCallback {
                override fun onStatus(status: OfflineRegionStatus?) = continuation.resume(status)

                override fun onError(error: String?) = continuation.resume(null)
            }
        )
    }

private suspend fun MapLibreRegion.awaitDelete(): Unit = suspendCancellableCoroutine { continuation ->
    delete(
        object : MapLibreRegion.OfflineRegionDeleteCallback {
            override fun onDelete() = continuation.resume(Unit)

            override fun onError(error: String) = continuation.resumeWithException(IllegalStateException(error))
        }
    )
}

private suspend fun MapLibreRegion.awaitInvalidate(): Unit = suspendCancellableCoroutine { continuation ->
    invalidate(
        object : MapLibreRegion.OfflineRegionInvalidateCallback {
            override fun onInvalidate() = continuation.resume(Unit)

            override fun onError(error: String) = continuation.resumeWithException(IllegalStateException(error))
        }
    )
}

private suspend fun MapLibreRegion.awaitUpdateMetadata(bytes: ByteArray): Unit =
    suspendCancellableCoroutine { continuation ->
        updateMetadata(
            bytes,
            object : MapLibreRegion.OfflineRegionUpdateMetadataCallback {
                override fun onUpdate(metadata: ByteArray) = continuation.resume(Unit)

                override fun onError(error: String) = continuation.resumeWithException(IllegalStateException(error))
            }
        )
    }
