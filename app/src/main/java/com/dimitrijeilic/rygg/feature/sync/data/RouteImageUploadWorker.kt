package com.dimitrijeilic.rygg.feature.sync.data

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dimitrijeilic.rygg.feature.library.data.RouteImageStorage
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageDao
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

// Uploads a route photo to Cloud Storage, then re-pushes its route so the doc starts listing it.
@HiltWorker
class RouteImageUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val imageStorage: RouteImageStorage,
    private val imageDao: RouteImageDao,
    private val storage: FirebaseStorage,
    private val syncManager: RouteSyncManager
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val imageId = inputData.getString(KEY_IMAGE_ID) ?: return Result.failure()
        val uid = inputData.getString(KEY_UID) ?: return Result.failure()

        val image = imageDao.getById(imageId) ?: return Result.success()
        if (image.fileUploaded) return Result.success()
        val file = imageStorage.resolve(imageId)
        if (!file.exists()) return Result.success()

        return try {
            storage.reference.child(RouteSyncManager.imagePath(uid, imageId))
                .putFile(Uri.fromFile(file), StorageMetadata.Builder().setContentType("image/jpeg").build())
                .await()
            imageDao.markUploaded(imageId)
            syncManager.onRouteChanged(image.entryId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_IMAGE_ID = "imageId"
        const val KEY_UID = "uid"
    }
}
