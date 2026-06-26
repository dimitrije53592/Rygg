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
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun ForgotPasswordScreen(params: ForgotPasswordScreenParams) {
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
        Text(text = stringResource(R.string.forgot_title), style = RyggTheme.typography.headlineLarge)

        OutlinedTextField(
            value = params.uiState.email,
            onValueChange = params.onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.auth_email)) }
        )

        if (params.uiState.emailSent) {
            Text(
                text = stringResource(R.string.forgot_sent),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.Success)
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
            text = stringResource(R.string.forgot_button),
            onClick = params.onSendClick,
            modifier = Modifier.fillMaxWidth(),
            isLoading = params.uiState.isLoading
        )

        TextButton(onClick = params.onBackClick) {
            Text(stringResource(R.string.forgot_to_login))
        }
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailSent: Boolean = false
)

data class ForgotPasswordScreenParams(
    val uiState: ForgotPasswordUiState,
    val onEmailChange: (String) -> Unit,
    val onSendClick: () -> Unit,
    val onBackClick: () -> Unit
)
