package com.example.rygg.feature.library.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
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

    suspend fun saveText(content: String): File = withContext(Dispatchers.IO) {
        val target = File(filesDir, "${Uuid.random()}.gpx")
        target.writeText(content)
        target
    }

    // Persist raw .gpx bytes pulled from the cloud into a fresh local file.
    suspend fun saveBytes(bytes: ByteArray): File = withContext(Dispatchers.IO) {
        val target = File(filesDir, "${Uuid.random()}.gpx")
        target.writeBytes(bytes)
        target
    }

    // Rename a stored file to a human-readable, sanitized, de-duplicated "<name>.gpx".
    // Returns the final file name (or the current one if the rename fails).
    suspend fun rename(currentFileName: String, desiredBaseName: String): String = withContext(Dispatchers.IO) {
        val finalName = uniqueFileName(sanitizeBaseName(desiredBaseName), excluding = currentFileName)
        if (finalName == currentFileName) return@withContext currentFileName
        val renamed = File(filesDir, currentFileName).renameTo(File(filesDir, finalName))
        if (renamed) finalName else currentFileName
    }

    private fun sanitizeBaseName(raw: String): String {
        val base = raw.substringBeforeLast(".gpx")
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .trim()
            .ifBlank { DEFAULT_BASE_NAME }
        return base.take(MAX_BASE_NAME_LENGTH)
    }

    // [excluding] is the file being renamed: it must not count as a collision with itself,
    // otherwise renaming a file to its own name would needlessly append "-1".
    private fun uniqueFileName(baseName: String, excluding: String? = null): String {
        var candidate = "$baseName.gpx"
        var index = 1
        while (candidate != excluding && File(filesDir, candidate).exists()) {
            candidate = "$baseName-$index.gpx"
            index++
        }
        return candidate
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

    suspend fun deleteFile(fileName: String): Boolean = withContext(Dispatchers.IO) {
        File(filesDir, fileName).delete()
    }

    fun resolve(fileName: String): File = File(filesDir, fileName)

    // Content Uri for a stored .gpx, usable in an ACTION_SEND share. Backed by the
    // FileProvider declared in the manifest (authority "<packageName>.fileprovider").
    fun shareUri(fileName: String): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", resolve(fileName))
}

data class StoredFile(
    val fileName: String,
    val uri: Uri
)

private const val GPX_FILES_FOLDER_NAME = "trips"
private const val DEFAULT_BASE_NAME = "recording"
private const val MAX_BASE_NAME_LENGTH = 60
