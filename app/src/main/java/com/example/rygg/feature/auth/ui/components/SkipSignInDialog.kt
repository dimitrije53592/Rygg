package com.example.rygg.feature.auth.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun SkipSignInDialog(
    onContinueAsGuest: () -> Unit,
    onSignIn: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onSignIn,
        title = { Text(text = stringResource(R.string.auth_skip_title)) },
        text = { Text(text = stringResource(R.string.auth_skip_message)) },
        confirmButton = {
            TextButton(onClick = onSignIn) {
                Text(
                    text = stringResource(R.string.auth_skip_sign_in),
                    color = RyggTheme.getColor(RyggColor.BrandGreen)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onContinueAsGuest) {
                Text(
                    text = stringResource(R.string.auth_skip_continue),
                    color = RyggTheme.getColor(RyggColor.TextSecondary)
                )
            }
        }
    )
}
