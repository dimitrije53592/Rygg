package com.example.rygg.feature.library.domain

// Where a route stands relative to the cloud. Drives the per-card sync badge and the
// push queue. Guest/offline routes stay LOCAL_ONLY until an account adopts them.
enum class SyncStatus {
    // Not linked to any account yet (guest, or signed-out).
    LOCAL_ONLY,

    // Owned by an account but metadata/file not fully pushed yet.
    PENDING_UPLOAD,

    // Metadata and file are mirrored to the cloud.
    SYNCED,

    // Soft-deleted locally; the tombstone still needs to propagate to the cloud.
    PENDING_DELETE
}
