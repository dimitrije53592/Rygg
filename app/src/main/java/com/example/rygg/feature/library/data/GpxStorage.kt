package com.example.rygg.feature.library.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.Uuid

class GpxStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val filesDir: File
        get() = File(context.filesDir, gpxFilesFolderName).apply { mkdirs() }

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
}

data class StoredFile(
    val fileName: String,
    val uri: Uri
)

private const val gpxFilesFolderName = "trips"