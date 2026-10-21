package com.dimitrijeilic.rygg.feature.sync.data

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.outcomeCatching
import com.dimitrijeilic.rygg.core.ui.utils.RouteShareLinks
import com.dimitrijeilic.rygg.feature.auth.data.AuthRepository
import com.dimitrijeilic.rygg.feature.library.data.GpxStorage
import com.dimitrijeilic.rygg.feature.library.data.RouteImageStorage
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryDao
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryEntity
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageDao
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageEntity
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import com.dimitrijeilic.rygg.feature.sync.data.remote.RemoteImage
import com.dimitrijeilic.rygg.feature.sync.data.remote.RemoteRoute
import com.dimitrijeilic.rygg.feature.sync.data.remote.SharedRoute
import com.dimitrijeilic.rygg.feature.sync.data.remote.toNewEntity
import com.dimitrijeilic.rygg.feature.sync.data.remote.toRemote
import com.dimitrijeilic.rygg.feature.sync.data.remote.toSavedCopyEntity
import com.dimitrijeilic.rygg.feature.sync.data.remote.toUpdatedEntity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

// Mirrors the local Room library to Firestore (metadata) + Cloud Storage (.gpx files, photos) for the
// signed-in account, and backs the "link -> save a copy" sharing flow. Offline-first: metadata
// writes ride Firestore's offline queue (no await), file transfers are WorkManager jobs.
@Singleton
class RouteSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val dao: GpxFileEntryDao,
    private val gpxStorage: GpxStorage,
    private val imageDao: RouteImageDao,
    private val imageStorage: RouteImageStorage,
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
        pushImageTransfers(uid)
    }

    // Drop the account's synced routes (safe in the cloud, re-pull next sign-in); unsynced ones
    // revert to guest rows so nothing is lost.
    suspend fun onSignedOut(previousUid: String) {
        stopPull()
        releaseRoutes(previousUid)
    }

    // Rows owned by an account that isn't signed in here, e.g. restored from an Android backup or
    // left by a kill before onSignedOut ran, would otherwise show in (and sync into) another
    // account's library. Treat each such account as signed out.
    suspend fun releaseForeignRoutes(currentUid: String?) {
        dao.getForeignOwners(currentUid).forEach { releaseRoutes(it) }
    }

    private suspend fun releaseRoutes(ownerUid: String) {
        (dao.getOwnedSynced(ownerUid) + dao.getOwnedPendingDelete(ownerUid)).forEach { deleteLocalRoute(it) }
        releaseImages(ownerUid, includeSynced = false)
        dao.revertOwnedUnsyncedToGuest(ownerUid)
    }

    // Photos of routes reverting to guest rows: drop the never-downloaded ones, and mark the rest
    // for re-upload under whichever account adopts the route next.
    private suspend fun releaseImages(ownerUid: String, includeSynced: Boolean) {
        imageDao.getForReverting(ownerUid, includeSynced).filter { !it.fileDownloaded }.forEach {
            workManager.cancelUniqueWork("image-download-${it.id}")
            imageDao.deleteById(it.id)
        }
        imageDao.resetUploaded(ownerUid, includeSynced)
    }

    // Removes a route row with its .gpx and photo files; Room cascades the photo rows.
    private suspend fun deleteLocalRoute(entity: GpxFileEntryEntity) {
        imageDao.getForEntry(entity.id).forEach { image ->
            workManager.cancelUniqueWork("image-upload-${image.id}")
            workManager.cancelUniqueWork("image-download-${image.id}")
            imageStorage.delete(image.id)
        }
        if (entity.fileName.isNotEmpty()) gpxStorage.deleteFile(entity.fileName)
        dao.deleteById(entity.id)
    }

    // Sync turned off while signed in: detach the pull, keep local data. onSignedIn resumes it.
    fun pauseSync() {
        stopPull()
    }

    private fun stopPull() {
        pullRegistration?.remove()
        pullRegistration = null
    }

    // --- Account deletion ---

    // Wipes everything the account owns in the cloud, then keeps its routes on this device as
    // guest rows. Runs before the auth user is deleted, since the rules need the uid signed in.
    // Every step reads from / waits on the server, so an offline or half-finished wipe surfaces
    // as an error instead of looking done.
    suspend fun deleteAccountData(uid: String) {
        stopPull()
        val owned = dao.getOwned(uid)
        owned.mapNotNull { it.remoteId }.forEach { remoteId ->
            workManager.cancelUniqueWork("upload-$remoteId")
            workManager.cancelUniqueWork("download-$remoteId")
        }
        owned.forEach { entity ->
            imageDao.getForEntry(entity.id).forEach { image ->
                workManager.cancelUniqueWork("image-upload-${image.id}")
                workManager.cancelUniqueWork("image-download-${image.id}")
            }
        }

        // Blob before snapshot: storage.rules checks the snapshot's ownerUid to allow the delete.
        sharedRoutes.whereEqualTo("ownerUid", uid).get(Source.SERVER).await().documents.forEach { doc ->
            doc.getString("sharedStoragePath")?.let { storage.reference.child(it).deleteIfExists() }
            doc.reference.delete().await()
        }

        // Include local remoteIds: a queued, not-yet-acked write would otherwise recreate its doc.
        val remoteIds = userRoutes(uid).get(Source.SERVER).await().documents.map { it.id } +
            owned.mapNotNull { it.remoteId }
        remoteIds.distinct().chunked(MAX_BATCH_WRITES).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(userRoutes(uid).document(it)) }
            batch.commit().await()
        }

        storage.reference.child("users/$uid").listAll().await().items.forEach { it.deleteIfExists() }
        storage.reference.child("users/$uid/images").listAll().await().items.forEach { it.deleteIfExists() }

        // Ghost rows (no local file) and pending tombstones have nothing left to keep.
        owned.filter { !it.fileDownloaded || it.syncStatus == "PENDING_DELETE" }.forEach { deleteLocalRoute(it) }
        releaseImages(uid, includeSynced = true)
        dao.revertOwnedToGuest(uid)
    }

    private suspend fun StorageReference.deleteIfExists() {
        try {
            delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    // Claim guest (ownerUid == null) routes: drop local dupes, link cloud dupes (no re-upload),
    // else mint a remoteId and let pushPending upload.
    private suspend fun adoptGuestRoutes(uid: String) {
        dao.getGuestRoutes().forEach { guest ->
            val localDupe = dao.getByContentHash(uid, guest.contentHash)
            if (localDupe != null) {
                deleteLocalRoute(guest)
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
        val images = imageDao.getForEntry(entity.id)
        val syncedImages = images.filter { it.fileUploaded }.map { it.toRemote() }
        userRoutes(uid).document(remoteId).set(entity.toRemote(remoteId, storagePath, syncedImages))
            .addOnSuccessListener {
                if (!needsFileUpload) scope.launch { dao.setSyncStatus(entity.id, "SYNCED") }
            }
        if (needsFileUpload) {
            // Any network: a small .gpx shouldn't wait for Wi-Fi, and a doc with no file is a
            // "ghost" on other devices. The Wi-Fi-only setting still gates bulk incoming downloads.
            enqueueUpload(entity.id, uid, remoteId, entity.fileName, wifiOnly = false)
        }
        if (settings.currentSyncPhotos()) {
            val wifiOnly = settings.currentSyncWifiOnly()
            images.filter { !it.fileUploaded && it.fileDownloaded }.forEach { enqueueImageUpload(uid, it.id, wifiOnly) }
        }
    }

    // Starts every photo transfer still owed for this account; photos wait for the "Sync photos"
    // setting, so this also runs when it is switched back on.
    suspend fun pushImageTransfers(uid: String) {
        if (!settings.currentSyncEnabled() || !settings.currentSyncPhotos()) return
        val wifiOnly = settings.currentSyncWifiOnly()
        imageDao.getPendingTransfers(uid).forEach { image ->
            if (image.fileDownloaded) enqueueImageUpload(uid, image.id, wifiOnly) else enqueueImageDownload(uid, image.id, wifiOnly)
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
            if (local != null) deleteLocalRoute(local)
            return
        }
        if (local == null) {
            val newId = dao.insert(remote.toNewEntity(uid))
            enqueueDownload(newId, uid, remote.remoteId, immediate = false, wifiOnly = settings.currentSyncWifiOnly())
            applyRemoteImages(uid, newId, remote.images)
        } else if (remote.updatedAt > local.updatedAt) {
            applyRemoteImages(uid, local.id, remote.images)
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

    // Make the route's photos match the doc. A local photo missing from the doc is dropped only if
    // it was uploaded (so another device deleted it); unuploaded ones are kept and go last.
    private suspend fun applyRemoteImages(uid: String, entryId: Long, remoteImages: List<RemoteImage>) {
        val local = imageDao.getForEntry(entryId)
        val remoteIds = remoteImages.map { it.id }.toSet()
        local.filter { it.fileUploaded && it.id !in remoteIds }.forEach { image ->
            workManager.cancelUniqueWork("image-download-${image.id}")
            imageStorage.delete(image.id)
            imageDao.deleteById(image.id)
        }
        val localIds = local.map { it.id }.toSet()
        val fresh = remoteImages.filter { it.id !in localIds }
        imageDao.insertAll(fresh.map { it.toNewEntity(entryId) })
        imageDao.setPositions(
            remoteImages.sortedBy { it.position }.map { it.id } +
                local.filter { !it.fileUploaded }.sortedBy { it.position }.map { it.id }
        )
        if (settings.currentSyncPhotos()) {
            val wifiOnly = settings.currentSyncWifiOnly()
            fresh.forEach { enqueueImageDownload(uid, it.id, wifiOnly) }
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

    // A photo was removed locally: re-push the route so the doc drops it.
    suspend fun onImageDeleted(image: RouteImageEntity) {
        workManager.cancelUniqueWork("image-upload-${image.id}")
        onRouteChanged(image.entryId)
        val uid = auth.currentUser()?.uid ?: return
        if (image.fileUploaded) runCatching { storage.reference.child(imagePath(uid, image.id)).delete() }
    }

    // Delete locally and, if owned, remove the cloud copies. An owned delete records a
    // PENDING_DELETE tombstone first so an interrupted delete is retried by pushPending.
    suspend fun deleteRoute(entry: GpxFileEntry) {
        val entity = dao.getById(entry.id) ?: return
        val uid = entity.ownerUid
        if (uid == null || entity.remoteId == null) {
            deleteLocalRoute(entity)
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
        imageDao.getForEntry(entity.id).filter { it.fileUploaded }.forEach { image ->
            runCatching { storage.reference.child(imagePath(uid, image.id)).delete() }
        }
        deleteLocalRoute(entity)
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
            pushImageTransfers(uid)
        }

        val token = Uuid.random().toString().replace("-", "")
        val sharedPath = "shared/$token.gpx"
        val file = gpxStorage.resolve(local.fileName)
        // Await these two: the returned link must resolve for a recipient immediately.
        val snapshot = sharedRoutes.document(token)
        snapshot.set(
            SharedRoute(
                token = token,
                ownerUid = uid,
                sharedStoragePath = sharedPath,
                route = local.toRemote(remoteId, "users/$uid/$remoteId.gpx")
            )
        ).await()
        try {
            storage.reference.child(sharedPath).putFile(Uri.fromFile(file)).await()
        } catch (e: Exception) {
            runCatching { snapshot.delete().await() }
            throw e
        }
        dao.setSharedToken(entry.id, token)
        RouteShareLinks.buildShareUrl(token)
    }

    suspend fun resolveSharedRoute(token: String): Outcome<SharedRoute> = outcomeCatching {
        val snapshot = sharedRoutes.document(token).get().await()
        snapshot.toObject(SharedRoute::class.java) ?: throw IllegalStateException(ERR_NOT_FOUND)
    }

    fun isOwnShare(shared: SharedRoute): Boolean =
        auth.currentUser()?.uid?.let { it == shared.ownerUid } ?: false

    suspend fun findLibraryCopy(shared: SharedRoute): Long? {
        val uid = auth.currentUser()?.uid
        val remoteId = shared.route.remoteId
        if (uid != null && shared.ownerUid == uid && remoteId.isNotEmpty()) {
            dao.getActiveIdByRemoteId(remoteId)?.let { return it }
        }
        val contentHash = shared.route.contentHash
        if (contentHash.isEmpty()) return null
        return dao.getActiveIdByContentHash(uid, contentHash)
    }

    suspend fun saveSharedCopy(shared: SharedRoute): Outcome<Long> = outcomeCatching {
        findLibraryCopy(shared)?.let { return@outcomeCatching it }
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

    private fun enqueueImageUpload(uid: String, imageId: String, wifiOnly: Boolean) {
        val network = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val request = OneTimeWorkRequestBuilder<RouteImageUploadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(network).build())
            .setInputData(
                workDataOf(
                    RouteImageUploadWorker.KEY_IMAGE_ID to imageId,
                    RouteImageUploadWorker.KEY_UID to uid
                )
            )
            .build()
        workManager.enqueueUniqueWork("image-upload-$imageId", ExistingWorkPolicy.KEEP, request)
    }

    private fun enqueueImageDownload(uid: String, imageId: String, wifiOnly: Boolean) {
        val network = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val request = OneTimeWorkRequestBuilder<RouteImageDownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(network).build())
            .setInputData(
                workDataOf(
                    RouteImageDownloadWorker.KEY_IMAGE_ID to imageId,
                    RouteImageDownloadWorker.KEY_UID to uid
                )
            )
            .build()
        workManager.enqueueUniqueWork("image-download-$imageId", ExistingWorkPolicy.KEEP, request)
    }

    companion object {
        const val ERR_SIGN_IN_REQUIRED = "SIGN_IN_REQUIRED"
        const val ERR_ROUTE_MISSING = "ROUTE_MISSING"
        const val ERR_FILE_NOT_READY = "FILE_NOT_READY"
        const val ERR_NOT_FOUND = "NOT_FOUND"

        private const val MAX_FILE_BYTES = 20L * 1024 * 1024

        // Mirrors the 5 MB cap in storage.rules; re-encoded 2048px JPEGs stay well under it.
        const val MAX_IMAGE_BYTES = 5L * 1024 * 1024

        // Flat per-account folder, so account deletion can sweep it with a single listAll.
        fun imagePath(uid: String, imageId: String) = "users/$uid/images/$imageId.jpg"

        // Firestore caps a write batch at 500 operations.
        private const val MAX_BATCH_WRITES = 500
    }
}
