package com.dimitrijeilic.rygg.feature.profile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.BuildConfig
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.auth.domain.SignInMethod
import com.dimitrijeilic.rygg.feature.profile.ui.screen.DeleteAccountError
import com.dimitrijeilic.rygg.feature.profile.ui.screen.DeleteAccountState

/**
 * Confirms account deletion. The re-authentication doubles as the deliberate second step: a
 * password account types its password here, a Google account confirms in the account picker
 * that the confirm button opens.
 */
@Composable
internal fun DeleteAccountDialog(
    state: DeleteAccountState.Confirming,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    RyggDialog(
        title = stringResource(R.string.profile_delete_title),
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.PersonRemove,
        iconTint = RyggTheme.getColor(RyggColor.Error),
        iconBackgroundColor = RyggTheme.getColor(RyggColor.ErrorSurface),
        message = stringResource(R.string.profile_delete_message)
    ) {
        when (state.method) {
            SignInMethod.PASSWORD -> RyggTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                isEnabled = !state.isDeleting,
                isPassword = true,
                labelText = stringResource(R.string.profile_delete_password_label),
                leadingIcon = painterResource(R.drawable.ic_password)
            )

            SignInMethod.GOOGLE -> Text(
                text = stringResource(R.string.profile_delete_google_hint),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
        }
        state.error?.let { error ->
            Text(
                text = stringResource(error.messageRes()),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.Error),
                textAlign = TextAlign.Center
            )
        }
        // The app keeps no logs, so a debug build is the only place to see what broke.
        if (BuildConfig.DEBUG && state.errorCause != null) {
            Text(
                text = state.errorCause,
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
        }
        RyggPrimaryButton(
            text = stringResource(R.string.profile_delete_confirm),
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            isEnabled = state.method == SignInMethod.GOOGLE || state.password.isNotEmpty(),
            isLoading = state.isDeleting,
            backgroundColor = RyggTheme.getColor(RyggColor.Error)
        )
        RyggSecondaryButton(
            text = stringResource(R.string.profile_delete_cancel),
            onClick = onDismiss,
            isEnabled = !state.isDeleting,
            textColor = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun DeleteAccountError.messageRes(): Int = when (this) {
    DeleteAccountError.WrongPassword -> R.string.profile_delete_error_password
    DeleteAccountError.WrongAccount -> R.string.profile_delete_error_account
    DeleteAccountError.Offline -> R.string.profile_delete_error_offline
    DeleteAccountError.Generic -> R.string.profile_delete_error_generic
}

@Preview
@Composable
private fun DeleteAccountDialogPreview() {
    RyggTheme {
        DeleteAccountDialog(
            state = DeleteAccountState.Confirming(
                method = SignInMethod.PASSWORD,
                password = "hunter2",
                error = DeleteAccountError.WrongPassword
            ),
            onPasswordChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}
