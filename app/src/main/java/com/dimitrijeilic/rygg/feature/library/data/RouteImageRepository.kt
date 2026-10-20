package com.dimitrijeilic.rygg.feature.library.data

import android.net.Uri
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.outcomeCatching
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageDao
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageEntity
import com.dimitrijeilic.rygg.feature.library.domain.MAX_IMAGES_PER_ROUTE
import com.dimitrijeilic.rygg.feature.library.domain.RouteImage
import com.dimitrijeilic.rygg.feature.sync.data.RouteSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RouteImageRepository @Inject constructor(
    private val imageDao: RouteImageDao,
    private val imageStorage: RouteImageStorage,
    private val routeSyncManager: RouteSyncManager
) {
    fun observeImages(entryId: Long): Flow<List<RouteImage>> =
        imageDao.observeForEntry(entryId).map { images -> images.map { it.toDomain() } }

    // Returns how many photos were added; picks beyond the per-route cap are dropped.
    suspend fun addImages(entryId: Long, uris: List<Uri>): Outcome<Int> = outcomeCatching {
        val existing = imageDao.getForEntry(entryId)
        val stored = uris.take(MAX_IMAGES_PER_ROUTE - existing.size).map { imageStorage.importFromUri(it) }
        if (stored.isEmpty()) return@outcomeCatching 0
        val now = System.currentTimeMillis()
        imageDao.insertAll(
            stored.mapIndexed { index, image ->
                RouteImageEntity(
                    id = image.id,
                    entryId = entryId,
                    position = existing.size + index,
                    width = image.width,
                    height = image.height,
                    createdAt = now
                )
            }
        )
        routeSyncManager.onRouteChanged(entryId)
        stored.size
    }

    suspend fun deleteImage(imageId: String): Outcome<Unit> = outcomeCatching {
        val image = imageDao.getById(imageId) ?: return@outcomeCatching
        imageDao.deleteById(imageId)
        imageStorage.delete(imageId)
        imageDao.setPositions(imageDao.getForEntry(image.entryId).map { it.id })
        routeSyncManager.onImageDeleted(image)
    }

    suspend fun reorder(entryId: Long, orderedIds: List<String>): Outcome<Unit> = outcomeCatching {
        imageDao.setPositions(orderedIds)
        routeSyncManager.onRouteChanged(entryId)
    }

    suspend fun setCover(entryId: Long, imageId: String): Outcome<Unit> {
        val ids = imageDao.getForEntry(entryId).map { it.id }
        return reorder(entryId, listOf(imageId) + (ids - imageId))
    }
}
