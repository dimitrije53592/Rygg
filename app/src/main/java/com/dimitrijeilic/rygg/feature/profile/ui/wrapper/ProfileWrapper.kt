package com.dimitrijeilic.rygg.feature.profile.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.core.ui.utils.rememberGoogleIdTokenRequest
import com.dimitrijeilic.rygg.feature.auth.domain.SignInMethod
import com.dimitrijeilic.rygg.feature.profile.ui.screen.DeleteAccountState
import com.dimitrijeilic.rygg.feature.profile.ui.screen.ProfileScreen
import com.dimitrijeilic.rygg.feature.profile.ui.screen.ProfileScreenParams
import com.dimitrijeilic.rygg.feature.profile.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileWrapper(
    onNavigateBack: () -> Unit,
    onAuthEntry: () -> Unit,
    onOpenSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val requestGoogleIdToken = rememberGoogleIdTokenRequest(reauthenticate = true)

    ProfileScreen(
        params = ProfileScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onSignOut = {
                viewModel.signOut()
                onAuthEntry()
            },
            onSignIn = onAuthEntry,
            onOpenSettings = onOpenSettings,
            onSendFeedback = onSendFeedback,
            onDeleteAccountClick = viewModel::onDeleteAccountClick,
            onDeleteAccountDismiss = viewModel::onDeleteAccountDismiss,
            onDeletePasswordChange = viewModel::onDeletePasswordChange,
            onConfirmDeleteAccount = {
                val state = uiState.deleteAccount as? DeleteAccountState.Confirming
                when (state?.method) {
                    SignInMethod.PASSWORD -> viewModel.onDeleteWithPassword()
                    // Google accounts re-authenticate through Credential Manager.
                    SignInMethod.GOOGLE -> scope.launch {
                        viewModel.onGoogleReauthStarted()
                        try {
                            val idToken = requestGoogleIdToken()
                            if (idToken != null) {
                                viewModel.onDeleteWithGoogle(idToken)
                            } else {
                                viewModel.onGoogleReauthFailed(cancelled = false)
                            }
                        } catch (e: GetCredentialException) {
                            viewModel.onGoogleReauthFailed(cancelled = e is GetCredentialCancellationException)
                        }
                    }

                    null -> Unit
                }
            }
        )
    )
}
