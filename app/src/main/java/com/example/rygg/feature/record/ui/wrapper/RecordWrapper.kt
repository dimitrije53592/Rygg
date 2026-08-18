package com.example.rygg.feature.record.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.record.ui.screen.RecordScreen
import com.example.rygg.feature.record.ui.screen.RecordScreenParams
import com.example.rygg.feature.record.ui.viewmodel.RecordViewModel

@Composable
fun RecordWrapper(
    onRecordingStopped: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecordScreen(
        params = RecordScreenParams(
            uiState = uiState,
            onSelectDiscipline = viewModel::onSelectDiscipline,
            onStart = viewModel::onStart,
            onPause = viewModel::onPause,
            onResume = viewModel::onResume,
            onStop = {
                viewModel.onStop()
                onRecordingStopped()
            },
            onAddWaypoint = viewModel::onAddWaypoint
        )
    )
}
