package com.dimitrijeilic.rygg.feature.auth.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.core.ui.utils.rememberGoogleIdTokenRequest
import com.dimitrijeilic.rygg.feature.auth.ui.screen.LoginScreen
import com.dimitrijeilic.rygg.feature.auth.ui.screen.LoginScreenParams
import com.dimitrijeilic.rygg.feature.auth.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginWrapper(
    onAuthSkipped: () -> Unit,
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val requestGoogleIdToken = rememberGoogleIdTokenRequest()

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoggedIn()
    }

    LoginScreen(
        params = LoginScreenParams(
            uiState = uiState,
            onSkipClick = onAuthSkipped,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = viewModel::login,
            onGoogleSignInClick = {
                scope.launch {
                    try {
                        requestGoogleIdToken()?.let(viewModel::signInWithGoogle)
                    } catch (e: GetCredentialCancellationException) {
                        // Closing the account chooser is a choice, not a failure.
                    } catch (e: GetCredentialException) {
                        viewModel.onGoogleSignInError(e.message)
                    }
                }
            },
            onRegisterClick = onNavigateToRegister,
            onForgotPasswordClick = onNavigateToForgotPassword
        )
    )
}
