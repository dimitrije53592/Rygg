package com.example.rygg.feature.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.auth.ui.screen.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }

    fun login() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.login(current.email.trim(), current.password)) {
                is Outcome.Success -> _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                is Outcome.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.cause.message) }
                Outcome.Loading -> Unit
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Outcome.Success -> _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                is Outcome.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.cause.message) }
                Outcome.Loading -> Unit
            }
        }
    }

    fun onGoogleSignInError(message: String?) = _uiState.update {
        it.copy(isLoading = false, errorMessage = message)
    }
}
