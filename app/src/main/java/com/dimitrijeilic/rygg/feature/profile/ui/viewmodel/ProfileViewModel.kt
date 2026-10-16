package com.dimitrijeilic.rygg.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.feature.auth.data.AuthRepository
import com.dimitrijeilic.rygg.feature.auth.domain.SignInMethod
import com.dimitrijeilic.rygg.feature.library.data.GpxFileEntryRepository
import com.dimitrijeilic.rygg.feature.profile.data.AccountDeletionRepository
import com.dimitrijeilic.rygg.feature.profile.domain.ReauthCredential
import com.dimitrijeilic.rygg.feature.profile.ui.screen.DeleteAccountError
import com.dimitrijeilic.rygg.feature.profile.ui.screen.DeleteAccountState
import com.dimitrijeilic.rygg.feature.profile.ui.screen.ProfileUiState
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountDeletionRepository: AccountDeletionRepository,
    gpxFileEntryRepository: GpxFileEntryRepository
) : ViewModel() {
    private val deleteAccount = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Hidden)

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.authState,
        gpxFileEntryRepository.observeGpxFileEntries(),
        deleteAccount
    ) { user, entries, deleteState ->
        ProfileUiState(
            isSignedIn = user != null,
            displayName = user?.displayName.orEmpty(),
            email = user?.email.orEmpty(),
            activityCount = entries.size,
            totalDistanceMeters = entries.sumOf { it.distanceMeters },
            totalAscentMeters = entries.sumOf { it.ascentMeters },
            deleteAccount = deleteState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = authRepository.currentUser().let { user ->
            ProfileUiState(
                isSignedIn = user != null,
                displayName = user?.displayName.orEmpty(),
                email = user?.email.orEmpty()
            )
        }
    )

    fun signOut() = authRepository.signOut()

    fun onDeleteAccountClick() {
        val method = authRepository.currentSignInMethod() ?: return
        deleteAccount.value = DeleteAccountState.Confirming(method = method)
    }

    fun onDeleteAccountDismiss() {
        // Mid-deletion the dialog stays up: leaving would hide whether the wipe finished.
        val current = deleteAccount.value
        if (current is DeleteAccountState.Confirming && current.isDeleting) return
        deleteAccount.value = DeleteAccountState.Hidden
    }

    fun onDeletePasswordChange(value: String) = updateConfirming { it.copy(password = value, error = null, errorCause = null) }

    fun onDeleteWithPassword() {
        val current = deleteAccount.value as? DeleteAccountState.Confirming ?: return
        if (current.password.isEmpty()) return
        delete(ReauthCredential.Password(current.password))
    }

    fun onDeleteWithGoogle(idToken: String) = delete(ReauthCredential.Google(idToken))

    fun onGoogleReauthStarted() = updateConfirming { it.copy(isDeleting = true, error = null, errorCause = null) }

    // A cancelled account picker is not an error — the user simply backed out.
    fun onGoogleReauthFailed(cancelled: Boolean) = updateConfirming {
        it.copy(isDeleting = false, error = if (cancelled) null else DeleteAccountError.Generic)
    }

    private fun delete(credential: ReauthCredential) {
        viewModelScope.launch {
            updateConfirming { it.copy(isDeleting = true, error = null, errorCause = null) }
            when (val outcome = accountDeletionRepository.deleteAccount(credential)) {
                is Outcome.Success -> deleteAccount.value = DeleteAccountState.Deleted
                is Outcome.Error -> updateConfirming {
                    it.copy(
                        isDeleting = false,
                        error = outcome.cause.toDeleteAccountError(it.method),
                        errorCause = outcome.cause.toString()
                    )
                }

                Outcome.Loading -> Unit
            }
        }
    }

    private fun updateConfirming(transform: (DeleteAccountState.Confirming) -> DeleteAccountState.Confirming) {
        deleteAccount.update { state ->
            if (state is DeleteAccountState.Confirming) transform(state) else state
        }
    }
}

private fun Throwable.toDeleteAccountError(method: SignInMethod): DeleteAccountError = when {
    this is FirebaseAuthInvalidCredentialsException && method == SignInMethod.PASSWORD ->
        DeleteAccountError.WrongPassword

    // Re-authenticating as a different Google account than the signed-in one.
    this is FirebaseAuthInvalidUserException -> DeleteAccountError.WrongAccount
    this is FirebaseNetworkException -> DeleteAccountError.Offline
    this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE ->
        DeleteAccountError.Offline

    else -> DeleteAccountError.Generic
}
