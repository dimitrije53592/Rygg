package com.dimitrijeilic.rygg.feature.offline.ui.wrapper

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.feature.offline.ui.screen.OfflineDownloadScreen
import com.dimitrijeilic.rygg.feature.offline.ui.screen.OfflineDownloadScreenParams
import com.dimitrijeilic.rygg.feature.offline.ui.viewmodel.OfflineDownloadViewModel

@Composable
fun OfflineDownloadWrapper(
    onNavigateBack: () -> Unit,
    onStarted: () -> Unit,
    viewModel: OfflineDownloadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val errorMessage = stringResource(R.string.offline_download_error)

    LaunchedEffect(Unit) {
        viewModel.started.collect { onStarted() }
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    OfflineDownloadScreen(
        params = OfflineDownloadScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onNameChange = viewModel::onNameChange,
            onFrameChanged = viewModel::onFrameChanged,
            onDownload = viewModel::onDownload
        )
    )
}
