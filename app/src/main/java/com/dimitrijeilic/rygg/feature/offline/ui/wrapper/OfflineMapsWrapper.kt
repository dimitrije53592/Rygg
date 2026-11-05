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
import com.dimitrijeilic.rygg.feature.offline.ui.screen.OfflineMapsScreen
import com.dimitrijeilic.rygg.feature.offline.ui.screen.OfflineMapsScreenParams
import com.dimitrijeilic.rygg.feature.offline.ui.viewmodel.OfflineMapsViewModel

@Composable
fun OfflineMapsWrapper(
    onNavigateBack: () -> Unit,
    onDownloadNewArea: () -> Unit,
    onOpenRegion: (Long) -> Unit,
    viewModel: OfflineMapsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val errorMessage = stringResource(R.string.offline_action_error)

    LaunchedEffect(Unit) {
        viewModel.error.collect {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    OfflineMapsScreen(
        params = OfflineMapsScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onDownloadNewArea = onDownloadNewArea,
            onOpenRegion = onOpenRegion,
            onResume = viewModel::onResume,
            onPause = viewModel::onPause,
            onUpdate = viewModel::onUpdate,
            onRename = viewModel::onRename,
            onDelete = viewModel::onDelete
        )
    )
}
