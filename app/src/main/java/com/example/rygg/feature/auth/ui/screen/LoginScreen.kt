package com.example.rygg.feature.auth.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTextField
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.auth.ui.components.TextDivider

@Composable
fun LoginScreen(params: LoginScreenParams) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RyggTheme.getColor(RyggColor.BrandGraphite)),
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RyggTheme.dimens.commonContentPadding24),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(R.string.login_skip),
                    color = RyggTheme.getColor(RyggColor.OnBrand),
                    style = RyggTheme.typography.headlineSmall,
                    modifier = Modifier.clickable {}
                )
            }
            Box(
                modifier = Modifier
                    .size(RyggTheme.dimens.iconSize100)
                    .clip(RoundedCornerShape(RyggTheme.dimens.radius24))
                    .background(RyggTheme.getColor(RyggColor.BrandDarkGreen)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logo),
                    tint = Color.Unspecified,
                    contentDescription = "",
                    modifier = Modifier.size(RyggTheme.dimens.iconSize80)
                )
            }
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing8))
            Text(
                text = stringResource(R.string.app_name),
                color = RyggTheme.getColor(RyggColor.OnBrand),
                style = RyggTheme.typography.headlineLarge
            )
        }
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RyggTheme.dimens.radius24))
                .background(RyggTheme.getColor(RyggColor.Background))
                .padding(RyggTheme.dimens.commonContentPadding24),
            verticalArrangement = Arrangement.spacedBy(
                RyggTheme.dimens.commonSpacing16,
                Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = RyggTheme.typography.headlineLarge,
                modifier = Modifier.padding(
                    vertical = RyggTheme.dimens.commonContentPadding8
                )
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
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing24))
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
