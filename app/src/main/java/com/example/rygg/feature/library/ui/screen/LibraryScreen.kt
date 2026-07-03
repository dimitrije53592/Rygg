package com.example.rygg.feature.library.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.library.ui.viewmodel.LibraryUiState

@Composable
fun LibraryScreen(params: LibraryScreenParams) {
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        params.onFilePicked(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        OutlinedButton(
            onClick = {
                filePicker.launch(SUPPORTED_FILE_TYPES)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.home_send_notification))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    RyggTheme {
        LibraryScreen(
            params = LibraryScreenParams(
                uiState = LibraryUiState(
                    gpxFileEntries = emptyList(),
                    isLoading = false
                ),
                onFilePicked = {}
            )
        )
    }
}

data class LibraryScreenParams(
    val uiState: LibraryUiState,
    val onFilePicked: (Uri) -> Unit
)

private val SUPPORTED_FILE_TYPES = arrayOf(
    "application/gpx+xml",
    "application/xml",
    "text/xml",
    "application/octet-stream"
)
