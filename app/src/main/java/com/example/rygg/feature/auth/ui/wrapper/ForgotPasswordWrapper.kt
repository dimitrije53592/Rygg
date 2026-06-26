package com.example.rygg.feature.auth.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.auth.ui.screen.ForgotPasswordScreen
import com.example.rygg.feature.auth.ui.screen.ForgotPasswordScreenParams
import com.example.rygg.feature.auth.ui.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordWrapper(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordScreen(
        params = ForgotPasswordScreenParams(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onSendClick = viewModel::sendReset,
            onBackClick = onNavigateBack
        )
    )
}
