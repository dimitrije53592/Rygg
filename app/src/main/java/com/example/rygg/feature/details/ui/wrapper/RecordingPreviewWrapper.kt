package com.example.rygg.feature.details.ui.wrapper

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.R
import com.example.rygg.feature.details.ui.screen.DetailsMode
import com.example.rygg.feature.details.ui.screen.DetailsScreen
import com.example.rygg.feature.details.ui.screen.DetailsScreenParams
import com.example.rygg.feature.details.ui.viewmodel.RecordingPreviewViewModel

@Composable
fun RecordingPreviewWrapper(
    onDone: () -> Unit,
    viewModel: RecordingPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val saveErrorMessage = stringResource(R.string.details_save_error)

    LaunchedEffect(Unit) {
        viewModel.finished.collect { onDone() }
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect {
            Toast.makeText(context, saveErrorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler { viewModel.onDiscard() }

    DetailsScreen(
        params = DetailsScreenParams(
            uiState = uiState,
            onNavigateBack = { viewModel.onDiscard() },
            mode = DetailsMode.SavePreview(
                name = name,
                onNameChange = { viewModel.onNameChange(it) },
                onSave = { viewModel.onSave() }
            )
        )
    )
}
