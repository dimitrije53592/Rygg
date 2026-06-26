package com.example.rygg.feature.auth.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun LoginScreen(params: LoginScreenParams) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding24),
        verticalArrangement = Arrangement.spacedBy(
            RyggTheme.dimens.commonSpacing16,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.login_title), style = RyggTheme.typography.headlineLarge)

        OutlinedTextField(
            value = params.uiState.email,
            onValueChange = params.onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.auth_email)) }
        )
        OutlinedTextField(
            value = params.uiState.password,
            onValueChange = params.onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            label = { Text(stringResource(R.string.auth_password)) }
        )

        params.uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.Error)
            )
        }

        RyggPrimaryButton(
            text = stringResource(R.string.login_button),
            onClick = params.onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            loading = params.uiState.isLoading
        )

        OutlinedButton(
            onClick = params.onGoogleSignInClick,
            enabled = !params.uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_google))
        }

        TextButton(onClick = params.onForgotPasswordClick) {
            Text(stringResource(R.string.login_forgot_password))
        }
        TextButton(onClick = params.onRegisterClick) {
            Text(stringResource(R.string.login_to_register))
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

data class LoginScreenParams(
    val uiState: LoginUiState,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onLoginClick: () -> Unit,
    val onGoogleSignInClick: () -> Unit,
    val onRegisterClick: () -> Unit,
    val onForgotPasswordClick: () -> Unit
)
