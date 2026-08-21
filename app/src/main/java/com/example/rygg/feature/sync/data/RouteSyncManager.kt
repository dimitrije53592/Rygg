package com.example.rygg.feature.sync.data

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.core.ui.utils.RouteShareLinks
import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.library.data.GpxStorage
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.settings.data.SettingsRepository
import com.example.rygg.feature.sync.data.remote.RemoteRoute
import com.example.rygg.feature.sync.data.remote.SharedRoute
import com.example.rygg.feature.sync.data.remote.toNewEntity
import com.example.rygg.feature.sync.data.remote.toRemote
import com.example.rygg.feature.sync.data.remote.toSavedCopyEntity
import com.example.rygg.feature.sync.data.remote.toUpdatedEntity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

// Mirrors the local Room library to Firestore (metadata) + Cloud Storage (.gpx files) for the
// signed-in account, and backs the "link -> save a copy" sharing flow. Offline-first: metadata
// writes ride Firestore's offline queue (no await), file transfers are WorkManager jobs.
@Singleton
class RouteSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val dao: GpxFileEntryDao,
    private val gpxStorage: GpxStorage,
    private val auth: AuthRepository,
    private val settings: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val workManager get() = WorkManager.getInstance(context)
    private var pullRegistration: ListenerRegistration? = null

    private fun userRoutes(uid: String) =
        firestore.collection("users").document(uid).collection("routes")

    private val sharedRoutes get() = firestore.collection("sharedRoutes")

    // --- Auth lifecycle (called by SyncInitializer) ---

    suspend fun onSignedIn(uid: String) {
        if (!settings.currentSyncEnabled()) return
        adoptGuestRoutes(uid)
        startPull(uid)
        pushPending(uid)
    }

    // Drop the account's synced routes (safe in the cloud, re-pull next sign-in); unsynced ones
    // revert to guest rows so nothing is lost.
    suspend fun onSignedOut(previousUid: String) {
        stopPull()
        (dao.getOwnedSynced(previousUid) + dao.getOwnedPendingDelete(previousUid)).forEach { entity ->
            if (entity.fileName.isNotEmpty()) gpxStorage.deleteFile(entity.fileName)
            dao.deleteById(entity.id)
        }
        dao.revertOwnedUnsyncedToGuest(previousUid)
    }

    // Sync turned off while signed in: detach the pull, keep local data. onSignedIn resumes it.
    fun pauseSync() {
        stopPull()
    }

    private fun stopPull() {
        pullRegistration?.remove()
        pullRegistration = null
    }

    // Claim guest (ownerUid == null) routes: drop local dupes, link cloud dupes (no re-upload),
    // else mint a remoteId and let pushPending upload.
    private suspend fun adoptGuestRoutes(uid: String) {
        dao.getGuestRoutes().forEach { guest ->
            val localDupe = dao.getByContentHash(uid, guest.contentHash)
            if (localDupe != null) {
                gpxStorage.deleteFile(guest.fileName)
                dao.deleteById(guest.id)
                return@forEach
            }
            val remoteMatch = findRemoteByContentHash(uid, guest.contentHash)
            if (remoteMatch != null) {
                dao.setOwnership(guest.id, uid, remoteMatch.remoteId, "SYNCED")
                dao.setFileUploaded(guest.id, true)
            } else {
                dao.setOwnership(guest.id, uid, Uuid.random().toString(), "PENDING_UPLOAD")
            }
        }
    }

    // Best-effort: offline (empty cache) returns null and we upload fresh instead of linking.
    private suspend fun findRemoteByContentHash(uid: String, contentHash: String): RemoteRoute? {
        if (contentHash.isEmpty()) return null
        return runCatching {
            userRoutes(uid)
                .whereEqualTo("contentHash", contentHash)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.let { doc -> doc.toObject(RemoteRoute::class.java)?.copy(remoteId = doc.id) }
        }.getOrNull()
    }

    // --- Push ---

    suspend fun pushPending(uid: String) {
        if (!settings.currentSyncEnabled()) return
        dao.getPendingSync(uid).forEach { entity ->
            if (entity.syncStatus == "PENDING_DELETE") pushDelete(uid, entity) else pushUpsert(uid, entity)
        }
    }

    private suspend fun pushUpsert(uid: String, entity: GpxFileEntryEntity) {
        if (!settings.currentSyncEnabled()) return
        val remoteId = entity.remoteId ?: Uuid.random().toString().also {
            dao.setOwnership(entity.id, uid, it, "PENDING_UPLOAD")
        }
        val storagePath = "users/$uid/$remoteId.gpx"
        // Skip the file upload on a metadata-only change (rename, favorite) — the .gpx is unchanged.
        val needsFileUpload =
            entity.fileDownloaded && entity.fileName.isNotEmpty() && !entity.fileUploaded
        // Eager, no-await metadata write (rides Firestore's offline queue). The row stays
        // PENDING_UPLOAD until the server acks, so the badge is only "backed up" once it truly is.
        userRoutes(uid).document(remoteId).set(entity.toRemote(remoteId, storagePath))
            .addOnSuccessListener {
                if (!needsFileUpload) scope.launch { dao.setSyncStatus(entity.id, "SYNCED") }
            }
        if (needsFileUpload) {
            // Any network: a small .gpx shouldn't wait for Wi-Fi, and a doc with no file is a
            // "ghost" on other devices. The Wi-Fi-only setting still gates bulk incoming downloads.
            enqueueUpload(entity.id, uid, remoteId, entity.fileName, wifiOnly = false)
        }
    }

    // --- Pull ---

    private fun startPull(uid: String) {
        stopPull()
        pullRegistration = userRoutes(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val changes = snapshot.documentChanges.toList()
            scope.launch { changes.forEach { runCatching { applyRemoteChange(uid, it) } } }
        }
    }

    private suspend fun applyRemoteChange(uid: String, change: DocumentChange) {
        val doc = change.document
        val remote = doc.toObject(RemoteRoute::class.java).copy(remoteId = doc.id)
        val local = dao.getByRemoteId(remote.remoteId)

        if (change.type == DocumentChange.Type.REMOVED || remote.deletedAt != null) {
            if (local != null) {
                gpxStorage.deleteFile(local.fileName)
                dao.deleteById(local.id)
            }
            return
        }
        if (local == null) {
            val newId = dao.insert(remote.toNewEntity(uid))
            enqueueDownload(newId, uid, remote.remoteId, immediate = false, wifiOnly = settings.currentSyncWifiOnly())
        } else if (remote.updatedAt > local.updatedAt) {
            val contentChanged =
                remote.contentHash.isNotEmpty() && remote.contentHash != local.contentHash
            // Last-write-wins: newer remote metadata replaces the local copy.
            dao.update(remote.toUpdatedEntity(local))
            if (contentChanged) {
                // Content changed in the cloud: re-fetch (stale file swept later by reconcile).
                dao.markFileMissing(local.id)
                enqueueDownload(local.id, uid, remote.remoteId, immediate = false, wifiOnly = settings.currentSyncWifiOnly())
            }
        }
    }

    // --- Local mutation hooks (called by the library repository) ---

    // Take ownership and push a newly-inserted route, or link to an existing cloud copy of the
    // same content instead of uploading a duplicate.
    suspend fun onRouteAdded(localId: Long) {
        val uid = auth.currentUser()?.uid ?: return
        val entity = dao.getById(localId) ?: return
        val remoteMatch = findRemoteByContentHash(uid, entity.contentHash)
        if (remoteMatch != null) {
            dao.setOwnership(localId, uid, remoteMatch.remoteId, "SYNCED")
            dao.setFileUploaded(localId, true)
            return
        }
        dao.setOwnership(localId, uid, Uuid.random().toString(), "PENDING_UPLOAD")
        val fresh = dao.getById(localId) ?: return
        pushUpsert(uid, fresh)
    }

    // A route's metadata changed (rename, favorite, …). Re-push if it's owned.
    suspend fun onRouteChanged(localId: Long) {
        val uid = auth.currentUser()?.uid ?: return
        val entity = dao.getById(localId) ?: return
        if (entity.ownerUid == null) return
        dao.markPendingUpload(localId, System.currentTimeMillis())
        val fresh = dao.getById(localId) ?: return
        pushUpsert(uid, fresh)
    }

    // Delete locally and, if owned, remove the cloud copies. An owned delete records a
    // PENDING_DELETE tombstone first so an interrupted delete is retried by pushPending.
    suspend fun deleteRoute(entry: GpxFileEntry) {
        val entity = dao.getById(entry.id) ?: return
        val uid = entity.ownerUid
        if (uid == null || entity.remoteId == null) {
            if (entity.fileName.isNotEmpty()) gpxStorage.deleteFile(entity.fileName)
            dao.deleteById(entity.id)
            return
        }
        dao.markPendingDelete(entity.id, System.currentTimeMillis())
        pushDelete(uid, entity)
    }

    // Firestore delete rides the offline queue (no await) and reaches other devices as REMOVED;
    // the blob delete is best-effort. The local row is dropped here so pushPending won't re-upsert.
    private suspend fun pushDelete(uid: String, entity: GpxFileEntryEntity) {
        entity.remoteId?.let { remoteId ->
            userRoutes(uid).document(remoteId).delete()
            runCatching { storage.reference.child("users/$uid/$remoteId.gpx").delete() }
        }
        if (entity.fileName.isNotEmpty()) gpxStorage.deleteFile(entity.fileName)
        dao.deleteById(entity.id)
    }

    // Force-fetch a pulled route's .gpx immediately (e.g. the user just opened it).
    suspend fun ensureFileDownloaded(localId: Long) {
        val entity = dao.getById(localId) ?: return
        val uid = entity.ownerUid ?: return
        val remoteId = entity.remoteId ?: return
        if (entity.fileDownloaded) return
        enqueueDownload(localId, uid, remoteId, immediate = true, wifiOnly = false)
    }

    // Force-upload an owned route's .gpx now (any network) to heal a "ghost" whose file never
    // reached the cloud. No-op if already uploaded, not owned, or no local file.
    suspend fun ensureFileUploaded(localId: Long) {
        if (!settings.currentSyncEnabled()) return
        val uid = auth.currentUser()?.uid ?: return
        val entity = dao.getById(localId) ?: return
        val remoteId = entity.remoteId
        if (remoteId == null ||
            entity.ownerUid != uid ||
            entity.fileUploaded ||
            !entity.fileDownloaded ||
            entity.fileName.isEmpty()
        ) {
            return
        }
        enqueueUpload(entity.id, uid, remoteId, entity.fileName, wifiOnly = false)
    }

    // --- Sharing (link -> save a copy) ---

    suspend fun createShareLink(entry: GpxFileEntry): Outcome<String> = outcomeCatching {
        val uid = auth.currentUser()?.uid ?: throw IllegalStateException(ERR_SIGN_IN_REQUIRED)
        var local = dao.getById(entry.id) ?: throw IllegalStateException(ERR_ROUTE_MISSING)
        if (!local.fileDownloaded || local.fileName.isEmpty()) {
            throw IllegalStateException(ERR_FILE_NOT_READY)
        }

        var remoteId = local.remoteId
        if (local.ownerUid == null || remoteId == null) {
            remoteId = Uuid.random().toString()
            dao.setOwnership(entry.id, uid, remoteId, "PENDING_UPLOAD")
            local = dao.getById(entry.id)!!
            userRoutes(uid).document(remoteId).set(local.toRemote(remoteId, "users/$uid/$remoteId.gpx"))
            enqueueUpload(entry.id, uid, remoteId, local.fileName, settings.currentSyncWifiOnly())
        }

        val token = Uuid.random().toString().replace("-", "")
        val sharedPath = "shared/$token.gpx"
        val file = gpxStorage.resolve(local.fileName)
        // Await these two: the returned link must resolve for a recipient immediately.
        storage.reference.child(sharedPath).putFile(Uri.fromFile(file)).await()
        sharedRoutes.document(token).set(
            SharedRoute(
                token = token,
                ownerUid = uid,
                sharedStoragePath = sharedPath,
                route = local.toRemote(remoteId, "users/$uid/$remoteId.gpx")
            )
        ).await()
        dao.setSharedToken(entry.id, token)
        RouteShareLinks.buildShareUrl(token)
    }

    suspend fun resolveSharedRoute(token: String): Outcome<SharedRoute> = outcomeCatching {
        val snapshot = sharedRoutes.document(token).get().await()
        snapshot.toObject(SharedRoute::class.java) ?: throw IllegalStateException(ERR_NOT_FOUND)
    }

    suspend fun saveSharedCopy(shared: SharedRoute): Outcome<Long> = outcomeCatching {
        val bytes = storage.reference.child(shared.sharedStoragePath).getBytes(MAX_FILE_BYTES).await()
        val file = gpxStorage.saveBytes(bytes)
        val uid = auth.currentUser()?.uid
        val now = System.currentTimeMillis()
        val id = dao.insert(shared.route.toSavedCopyEntity(file.name, uid, now))
        if (uid != null) onRouteAdded(id)
        id
    }

    // --- WorkManager helpers ---

    private fun enqueueUpload(
        entryId: Long,
        uid: String,
        remoteId: String,
        fileName: String,
        wifiOnly: Boolean
    ) {
        val network = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val request = OneTimeWorkRequestBuilder<RouteFileUploadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(network).build())
            .setInputData(
                workDataOf(
                    RouteFileUploadWorker.KEY_ENTRY_ID to entryId,
                    RouteFileUploadWorker.KEY_UID to uid,
                    RouteFileUploadWorker.KEY_REMOTE_ID to remoteId,
                    RouteFileUploadWorker.KEY_FILE_NAME to fileName
                )
            )
            .build()
        workManager.enqueueUniqueWork("upload-$remoteId", ExistingWorkPolicy.REPLACE, request)
    }

    private fun enqueueDownload(
        entryId: Long,
        uid: String,
        remoteId: String,
        immediate: Boolean,
        wifiOnly: Boolean
    ) {
        val network = when {
            immediate -> NetworkType.CONNECTED
            wifiOnly -> NetworkType.UNMETERED
            else -> NetworkType.CONNECTED
        }
        val request = OneTimeWorkRequestBuilder<RouteFileDownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(network).build())
            .setInputData(
                workDataOf(
                    RouteFileDownloadWorker.KEY_ENTRY_ID to entryId,
                    RouteFileDownloadWorker.KEY_UID to uid,
                    RouteFileDownloadWorker.KEY_REMOTE_ID to remoteId
                )
            )
            .build()
        val policy = if (immediate) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        workManager.enqueueUniqueWork("download-$remoteId", policy, request)
    }

    companion object {
        const val ERR_SIGN_IN_REQUIRED = "SIGN_IN_REQUIRED"
        const val ERR_ROUTE_MISSING = "ROUTE_MISSING"
        const val ERR_FILE_NOT_READY = "FILE_NOT_READY"
        const val ERR_NOT_FOUND = "NOT_FOUND"

        private const val MAX_FILE_BYTES = 20L * 1024 * 1024
    }
}
