package com.example.rygg.feature.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.auth.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val currentUser: StateFlow<User?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = authRepository.currentUser()
        )

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun signOut() = authRepository.signOut()
}
