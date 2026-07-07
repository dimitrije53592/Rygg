package com.example.rygg.feature.library.ui.screen

import android.net.Uri
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
import com.example.rygg.core.ui.utils.rememberFilePicker
import com.example.rygg.feature.library.ui.viewmodel.LibraryUiState

@Composable
fun LibraryScreen(params: LibraryScreenParams) {
    val launchFilePicker = rememberFilePicker(onFilePicked = params.onFilePicked)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        OutlinedButton(
            onClick = { launchFilePicker() },
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
