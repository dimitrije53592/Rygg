package com.dimitrijeilic.rygg.core.ui.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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

const val GPX_MIME_TYPE = "application/gpx+xml"

private val SUPPORTED_FILE_TYPES = arrayOf(
    GPX_MIME_TYPE,
    "application/xml",
    "text/xml",
    "application/octet-stream"
)
