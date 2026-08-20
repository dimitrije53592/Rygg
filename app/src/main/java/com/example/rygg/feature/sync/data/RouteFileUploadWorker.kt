package com.example.rygg.feature.sync.data

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rygg.feature.library.data.GpxStorage
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.google.firebase.storage.FirebaseStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

// Uploads a route's .gpx bytes to Cloud Storage, then flips the row to SYNCED. Runs under a
// Wi-Fi (UNMETERED) constraint so large files don't burn mobile data (the "files smart" policy).
@HiltWorker
class RouteFileUploadWorker @AssistedInject constructor(
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
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        if (entryId < 0) return Result.failure()

        val file = gpxStorage.resolve(fileName)
        if (!file.exists()) return Result.success()

        return try {
            storage.reference.child("users/$uid/$remoteId.gpx")
                .putFile(Uri.fromFile(file))
                .await()
            dao.setSyncStatus(entryId, "SYNCED")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_ENTRY_ID = "entryId"
        const val KEY_UID = "uid"
        const val KEY_REMOTE_ID = "remoteId"
        const val KEY_FILE_NAME = "fileName"
    }
}
