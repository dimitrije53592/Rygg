package com.example.rygg.feature.feedback.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.feedback.ui.screen.FeedbackScreen
import com.example.rygg.feature.feedback.ui.screen.FeedbackScreenParams
import com.example.rygg.feature.feedback.ui.viewmodel.FeedbackViewModel

@Composable
fun FeedbackWrapper(
    onNavigateBack: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedbackScreen(
        params = FeedbackScreenParams(
            uiState = uiState,
            onMessageChanged = { viewModel.onMessageChanged(it) },
            onCategorySelected = { viewModel.onCategorySelected(it) },
            onSend = { viewModel.onSend() },
            onDone = onNavigateBack
        )
    )
}
