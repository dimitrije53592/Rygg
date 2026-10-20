package com.dimitrijeilic.rygg.core.ui.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberFilePicker(
    onFilePicked: (Uri) -> Unit
): () -> Unit {
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onFilePicked(it) }
    }

    return { filePicker.launch(SUPPORTED_FILE_TYPES) }
}

@Composable
fun rememberDocumentCreator(
    mimeType: String,
    onCreated: (Uri) -> Unit
): (suggestedName: String) -> Unit {
    val creator = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType)
    ) { uri: Uri? ->
        uri?.let(onCreated)
    }

    return { suggestedName -> creator.launch(suggestedName) }
}

// System Photo Picker, so no storage permission is needed. The multi-select contract rejects a
// limit below 2, so a single remaining slot falls back to the single-photo picker.
@Composable
fun rememberPhotoPicker(
    maxItems: Int,
    onPicked: (List<Uri>) -> Unit
): () -> Unit {
    val multiPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems.coerceAtLeast(2))
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) onPicked(uris)
    }
    val singlePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onPicked(listOf(it)) }
    }
    val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    return {
        when {
            maxItems >= 2 -> multiPicker.launch(request)
            maxItems == 1 -> singlePicker.launch(request)
        }
    }
}

const val GPX_MIME_TYPE = "application/gpx+xml"

private val SUPPORTED_FILE_TYPES = arrayOf(
    GPX_MIME_TYPE,
    "application/xml",
    "text/xml",
    "application/octet-stream"
)
