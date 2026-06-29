package com.example.rygg.feature.auth.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.auth.ui.screen.RegisterScreen
import com.example.rygg.feature.auth.ui.screen.RegisterScreenParams
import com.example.rygg.feature.auth.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterWrapper(
    onRegistered: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) onRegistered()
    }

    RegisterScreen(
        params = RegisterScreenParams(
            uiState = uiState,
            onNameChange = viewModel::onNameChange,
            onSurnameChange = viewModel::onSurnameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onRegisterClick = viewModel::register,
            onBackClick = onNavigateBack
        )
    )
}
