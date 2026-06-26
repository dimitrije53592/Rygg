package com.example.rygg.feature.auth.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun RegisterScreen(params: RegisterScreenParams) {
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
        Text(text = stringResource(R.string.register_title), style = RyggTheme.typography.headlineLarge)

        OutlinedTextField(
            value = params.uiState.name,
            onValueChange = params.onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.auth_name)) }
        )
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
            text = stringResource(R.string.register_button),
            onClick = params.onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            isLoading = params.uiState.isLoading
        )

        TextButton(onClick = params.onBackClick) {
            Text(stringResource(R.string.register_to_login))
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

data class RegisterScreenParams(
    val uiState: RegisterUiState,
    val onNameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onRegisterClick: () -> Unit,
    val onBackClick: () -> Unit
)
