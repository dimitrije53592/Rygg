package com.dimitrijeilic.rygg.feature.sync.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dimitrijeilic.rygg.feature.library.data.RouteImageStorage
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageDao
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

// Fetches a photo listed in a pulled route doc into filesDir/images.
@HiltWorker
class RouteImageDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val imageStorage: RouteImageStorage,
    private val imageDao: RouteImageDao,
    private val storage: FirebaseStorage
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val imageId = inputData.getString(KEY_IMAGE_ID) ?: return Result.failure()
        val uid = inputData.getString(KEY_UID) ?: return Result.failure()

        val image = imageDao.getById(imageId) ?: return Result.success()
        if (image.fileDownloaded) return Result.success()

        return try {
            val bytes = storage.reference.child(RouteSyncManager.imagePath(uid, imageId))
                .getBytes(RouteSyncManager.MAX_IMAGE_BYTES)
                .await()
            imageStorage.saveBytes(imageId, bytes)
            imageDao.markDownloaded(imageId)
            Result.success()
        } catch (e: StorageException) {
            // Deleted on another device before we got to it; the next pull drops the row.
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_IMAGE_ID = "imageId"
        const val KEY_UID = "uid"
    }
}
