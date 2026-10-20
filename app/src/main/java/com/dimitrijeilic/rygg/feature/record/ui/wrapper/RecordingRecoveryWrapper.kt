package com.dimitrijeilic.rygg.feature.record.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.feature.record.ui.components.RecordingRecoveryDialog
import com.dimitrijeilic.rygg.feature.record.ui.viewmodel.RecordingRecoveryViewModel

@Composable
fun RecordingRecoveryWrapper(
    onOpenPreview: () -> Unit,
    viewModel: RecordingRecoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.openPreview.collect { onOpenPreview() }
    }

    uiState.pending?.let { summary ->
        RecordingRecoveryDialog(
            summary = summary,
            onResume = viewModel::onResume,
            onSave = viewModel::onSave,
            onDiscard = viewModel::onDiscard,
            onDismiss = viewModel::onDismiss
        )
    }
}
