package com.dimitrijeilic.rygg.feature.library.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException

// Serves route photos as content://<pkg>.images/image/{id}, so the UI, Coil and share intents
// address a photo by its synced id rather than by a device-specific file path. Read-only.
class RouteImageProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String? = if (imageId(uri) != null) MIME_TYPE else null

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw SecurityException("Route images are read-only")
        val file = fileFor(uri)
        if (!file.exists()) throw FileNotFoundException(uri.toString())
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val file = fileFor(uri)
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val values = columns.map { column ->
            when (column) {
                OpenableColumns.DISPLAY_NAME -> file.name
                OpenableColumns.SIZE -> file.length()
                else -> null
            }
        }
        return MatrixCursor(columns, 1).apply { addRow(values) }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException()

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException()

    private fun fileFor(uri: Uri): File {
        val id = imageId(uri) ?: throw FileNotFoundException(uri.toString())
        return RouteImageStorage.fileFor(requireNotNull(context), id)
    }

    private fun imageId(uri: Uri): String? =
        uri.pathSegments
            .takeIf { it.size == 2 && it[0] == IMAGE_PATH }
            ?.get(1)
            ?.takeIf { ID_PATTERN.matches(it) }

    companion object {
        fun uriFor(context: Context, imageId: String): Uri = Uri.Builder()
            .scheme("content")
            .authority("${context.packageName}.images")
            .appendPath(IMAGE_PATH)
            .appendPath(imageId)
            .build()

        private const val IMAGE_PATH = "image"
        private const val MIME_TYPE = "image/jpeg"

        // Rejects path tricks like "../" before the id reaches the filesystem.
        private val ID_PATTERN = Regex("[0-9a-fA-F-]{36}")
    }
}
