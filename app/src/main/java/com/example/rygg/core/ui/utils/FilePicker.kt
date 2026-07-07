package com.example.rygg.core.ui.utils

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

private val SUPPORTED_FILE_TYPES = arrayOf(
    "application/gpx+xml",
    "application/xml",
    "text/xml",
    "application/octet-stream"
)
