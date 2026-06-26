package com.example.rygg.feature.auth.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.R
import com.example.rygg.feature.auth.ui.screen.LoginScreen
import com.example.rygg.feature.auth.ui.screen.LoginScreenParams
import com.example.rygg.feature.auth.ui.viewmodel.LoginViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginWrapper(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoggedIn()
    }

    LoginScreen(
        params = LoginScreenParams(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = viewModel::login,
            onGoogleSignInClick = {
                scope.launch {
                    try {
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        val result = credentialManager.getCredential(context, request)
                        val credential = result.credential
                        if (credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                            viewModel.signInWithGoogle(idToken)
                        }
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
