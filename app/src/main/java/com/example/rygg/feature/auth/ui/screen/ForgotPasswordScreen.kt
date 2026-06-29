package com.example.rygg.feature.auth.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTextField
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.auth.ui.components.AuthScaffold

@Composable
fun ForgotPasswordScreen(params: ForgotPasswordScreenParams) {
    AuthScaffold {
        if (params.uiState.emailSent) {
            SentConfirmation(params)
        } else {
            ResetForm(params)
        }
    }
}

@Composable
private fun ResetForm(params: ForgotPasswordScreenParams) {
    Text(
        text = stringResource(R.string.forgot_title),
        style = RyggTheme.typography.headlineLarge,
        color = RyggTheme.getColor(RyggColor.TextPrimary),
        modifier = Modifier.padding(bottom = RyggTheme.dimens.commonContentPadding8)
    )
    Text(
        text = stringResource(R.string.forgot_lead),
        style = RyggTheme.typography.bodyMedium,
        color = RyggTheme.getColor(RyggColor.TextSecondary),
        textAlign = TextAlign.Center
    )
    RyggTextField(
        value = params.uiState.email,
        onValueChange = params.onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        labelText = stringResource(R.string.auth_email)
    )

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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.forgot_remembered),
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
        Text(
            text = stringResource(R.string.login_button),
            color = RyggTheme.getColor(RyggColor.BrandGreen),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { params.onBackClick() }
        )
    }
}

@Composable
private fun SentConfirmation(params: ForgotPasswordScreenParams) {
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.iconSize64)
            .clip(CircleShape)
            .background(RyggTheme.getColor(RyggColor.BrandGreen).copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            tint = RyggTheme.getColor(RyggColor.BrandGreen),
            contentDescription = null,
            modifier = Modifier.size(RyggTheme.dimens.iconSize32)
        )
    }
    Text(
        text = stringResource(R.string.forgot_sent_title),
        style = RyggTheme.typography.headlineLarge,
        color = RyggTheme.getColor(RyggColor.TextPrimary)
    )
    Text(
        text = stringResource(R.string.forgot_sent_body, params.uiState.email),
        style = RyggTheme.typography.bodyMedium,
        color = RyggTheme.getColor(RyggColor.TextSecondary),
        textAlign = TextAlign.Center
    )
    RyggPrimaryButton(
        text = stringResource(R.string.forgot_to_login),
        onClick = params.onBackClick,
        modifier = Modifier.fillMaxWidth()
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.forgot_didnt_get),
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
        Text(
            text = stringResource(R.string.forgot_resend),
            color = RyggTheme.getColor(RyggColor.BrandGreen),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { params.onSendClick() }
        )
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
