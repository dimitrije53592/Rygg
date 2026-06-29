package com.example.rygg.feature.auth.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTextField
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.auth.ui.components.AuthScaffold

@Composable
fun RegisterScreen(params: RegisterScreenParams) {
    AuthScaffold(showSkip = true) {
        Text(
            text = stringResource(R.string.register_title),
            style = RyggTheme.typography.headlineLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.padding(bottom = RyggTheme.dimens.commonContentPadding8)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                RyggTextField(
                    value = params.uiState.name,
                    onValueChange = params.onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    labelText = stringResource(R.string.auth_name)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                RyggTextField(
                    value = params.uiState.surname,
                    onValueChange = params.onSurnameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    labelText = stringResource(R.string.auth_surname)
                )
            }
        }
        RyggTextField(
            value = params.uiState.email,
            onValueChange = params.onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            labelText = stringResource(R.string.auth_email)
        )
        RyggTextField(
            value = params.uiState.password,
            onValueChange = params.onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isPassword = true,
            labelText = stringResource(R.string.auth_password)
        )
        RyggTextField(
            value = params.uiState.confirmPassword,
            onValueChange = params.onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isPassword = true,
            labelText = stringResource(R.string.auth_confirm_password)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.register_have_account),
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
            Text(
                text = stringResource(R.string.login_button),
                color = RyggTheme.getColor(RyggColor.BrandGreen),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    params.onBackClick()
                }
            )
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

data class RegisterScreenParams(
    val uiState: RegisterUiState,
    val onNameChange: (String) -> Unit,
    val onSurnameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onRegisterClick: () -> Unit,
    val onBackClick: () -> Unit
)
