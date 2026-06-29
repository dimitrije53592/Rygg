package com.example.rygg.feature.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onSurnameChange(value: String) = _uiState.update { it.copy(surname = value) }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordMismatch = false) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, passwordMismatch = false) }

    fun register() {
        val current = _uiState.value
        if (current.password != current.confirmPassword) {
            _uiState.update { it.copy(passwordMismatch = true, errorMessage = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, passwordMismatch = false) }

            when (val result = authRepository.register(current.name.trim(), current.email.trim(), current.password)) {
                is Outcome.Success -> _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                is Outcome.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.cause.message) }
                Outcome.Loading -> Unit
            }
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordMismatch: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)
