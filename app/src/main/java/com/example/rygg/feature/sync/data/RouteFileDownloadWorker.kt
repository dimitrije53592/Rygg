package com.example.rygg.feature.sync.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rygg.feature.library.data.GpxStorage
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.google.firebase.storage.FirebaseStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

// Downloads a route's .gpx bytes for a row that was pulled from another device, writes them
// to local storage, and records the on-disk filename (fileDownloaded flips to true).
@HiltWorker
class RouteFileDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val gpxStorage: GpxStorage,
    private val dao: GpxFileEntryDao,
    private val storage: FirebaseStorage
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entryId = inputData.getLong(KEY_ENTRY_ID, -1L)
        val uid = inputData.getString(KEY_UID) ?: return Result.failure()
        val remoteId = inputData.getString(KEY_REMOTE_ID) ?: return Result.failure()
        if (entryId < 0) return Result.failure()

        val local = dao.getById(entryId) ?: return Result.success()
        if (local.fileDownloaded) return Result.success()

        return try {
            val bytes = storage.reference.child("users/$uid/$remoteId.gpx")
                .getBytes(MAX_FILE_BYTES)
                .await()
            val file = gpxStorage.saveBytes(bytes)
            dao.setDownloadedFile(entryId, file.name)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_ENTRY_ID = "entryId"
        const val KEY_UID = "uid"
        const val KEY_REMOTE_ID = "remoteId"

        // Upper bound for an in-memory .gpx download (20 MB); real tracks are far smaller.
        private const val MAX_FILE_BYTES = 20L * 1024 * 1024
    }
}
