package com.example.rygg.feature.feedback.ui.wrapper

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.R
import com.example.rygg.feature.feedback.ui.screen.FeedbackScreen
import com.example.rygg.feature.feedback.ui.screen.FeedbackScreenParams
import com.example.rygg.feature.feedback.ui.viewmodel.FeedbackEvent
import com.example.rygg.feature.feedback.ui.viewmodel.FeedbackViewModel

@Composable
fun FeedbackWrapper(
    onNavigateBack: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sentMessage = stringResource(R.string.feedback_sent_toast)
    val failedMessage = stringResource(R.string.feedback_failed_toast)
    val throttledMessage = stringResource(R.string.feedback_throttled_toast)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FeedbackEvent.Sent -> {
                    Toast.makeText(context, sentMessage, Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }

                FeedbackEvent.Failed ->
                    Toast.makeText(context, failedMessage, Toast.LENGTH_LONG).show()

                FeedbackEvent.Throttled ->
                    Toast.makeText(context, throttledMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    FeedbackScreen(
        params = FeedbackScreenParams(
            uiState = uiState,
            onMessageChanged = { viewModel.onMessageChanged(it) },
            onCategorySelected = { viewModel.onCategorySelected(it) },
            onSend = { viewModel.onSend() }
        )
    )
}
