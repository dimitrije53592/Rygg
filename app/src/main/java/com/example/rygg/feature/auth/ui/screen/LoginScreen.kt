package com.example.rygg.feature.auth.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTextField
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.auth.ui.components.AuthScaffold
import com.example.rygg.feature.auth.ui.components.TextDivider

@Composable
fun LoginScreen(params: LoginScreenParams) {
    AuthScaffold(showSkip = true) {
        Text(
            text = stringResource(R.string.login_title),
            style = RyggTheme.typography.headlineLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.padding(bottom = RyggTheme.dimens.commonContentPadding8)
        )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(R.string.login_forgot_password),
                color = RyggTheme.getColor(RyggColor.BrandGreen),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    params.onForgotPasswordClick()
                }
            )
        }
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
            isLoading = params.uiState.isLoading
        )
        TextDivider(text = stringResource(R.string.login_or))
        RyggPrimaryButton(
            text = stringResource(R.string.login_google),
            onClick = params.onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth(),
            isEnabled = !params.uiState.isLoading,
            textColor = RyggTheme.getColor(RyggColor.TextPrimary),
            backgroundColor = RyggTheme.getColor(RyggColor.Background),
            leadingIcon = painterResource(R.drawable.ic_google_sign_in),
            leadingIconTint = Color.Unspecified,
            borderWidth = RyggTheme.dimens.border2
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_new_to_rygg),
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
            Text(
                text = stringResource(R.string.register_title),
                color = RyggTheme.getColor(RyggColor.BrandGreen),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    params.onRegisterClick()
                }
            )
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
