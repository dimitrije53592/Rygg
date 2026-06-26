package com.example.rygg.feature.profile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun ProfileScreen(params: ProfileScreenParams) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding24),
        verticalArrangement = Arrangement.spacedBy(
            RyggTheme.dimens.commonSpacing12,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = params.uiState.displayName.ifEmpty { stringResource(R.string.profile_no_name) },
            style = RyggTheme.typography.headlineSmall
        )
        Text(
            text = params.uiState.email,
            style = RyggTheme.typography.bodyMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        TextButton(onClick = params.onSignOut) {
            Text(stringResource(R.string.profile_sign_out))
        }
    }
}

data class ProfileUiState(
    val displayName: String = "",
    val email: String = ""
)

data class ProfileScreenParams(
    val uiState: ProfileUiState,
    val onSignOut: () -> Unit
)
