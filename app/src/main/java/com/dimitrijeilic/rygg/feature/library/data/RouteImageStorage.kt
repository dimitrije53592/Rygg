package com.dimitrijeilic.rygg.feature.library.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

class RouteImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File
        get() = File(context.filesDir, IMAGES_FOLDER_NAME).apply { mkdirs() }

    fun resolve(imageId: String): File = File(imagesDir, "$imageId.jpg")

    // Re-encoding through a Bitmap is what strips EXIF (including GPS) from the stored copy; Coil
    // applies the EXIF rotation while decoding, which ImageDecoder can't do below API 28.
    suspend fun importFromUri(uri: Uri): StoredImage = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(MAX_EDGE_PX)
            .scale(Scale.FIT)
            .precision(Precision.INEXACT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()
        val result = context.imageLoader.execute(request) as? SuccessResult
            ?: throw IOException("Cannot decode $uri")
        val bitmap = result.image.toBitmap().downscaledTo(MAX_EDGE_PX)
        val id = Uuid.random().toString()
        resolve(id).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        StoredImage(id = id, width = bitmap.width, height = bitmap.height)
    }

    suspend fun saveBytes(imageId: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        resolve(imageId).writeBytes(bytes)
    }

    suspend fun delete(imageId: String): Boolean = withContext(Dispatchers.IO) {
        resolve(imageId).delete()
    }

    suspend fun listStoredIds(): List<String> = withContext(Dispatchers.IO) {
        imagesDir.listFiles()
            ?.filter { it.isFile }
            ?.map { it.nameWithoutExtension }
            .orEmpty()
    }

    private fun Bitmap.downscaledTo(maxEdge: Int): Bitmap {
        val longEdge = max(width, height)
        if (longEdge <= maxEdge) return this
        val factor = maxEdge.toFloat() / longEdge
        return Bitmap.createScaledBitmap(this, (width * factor).roundToInt(), (height * factor).roundToInt(), true)
    }
}

data class StoredImage(
    val id: String,
    val width: Int,
    val height: Int
)

private const val IMAGES_FOLDER_NAME = "images"
private const val MAX_EDGE_PX = 2048
private const val JPEG_QUALITY = 85
