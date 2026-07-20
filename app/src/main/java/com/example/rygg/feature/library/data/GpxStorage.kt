package com.example.rygg.feature.library.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.uuid.Uuid

class GpxStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val filesDir: File
        get() = File(context.filesDir, GPX_FILES_FOLDER_NAME).apply { mkdirs() }

    suspend fun listedStoredFiles(): List<StoredFile> = withContext(Dispatchers.IO) {
        filesDir.listFiles()
            ?.filter { it.isFile }
            ?.map { StoredFile(fileName = it.name, uri = it.toUri()) }
            .orEmpty()
    }

    suspend fun saveFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val target = File(filesDir, "${Uuid.random()}.gpx")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        target
    }

    suspend fun sha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read = input.read(buffer)
            while (read >= 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun originalDisplayName(uri: Uri): String? =
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
}

data class StoredFile(
    val fileName: String,
    val uri: Uri
)

private const val GPX_FILES_FOLDER_NAME = "trips"
