package com.example.rygg.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.profile.ui.screen.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = authRepository.authState
        .map { user ->
            ProfileUiState(
                isSignedIn = user != null,
                displayName = user?.displayName.orEmpty(),
                email = user?.email.orEmpty()
            )
        }
        .stateIn(
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
}
