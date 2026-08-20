package com.example.rygg.feature.profile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggCard
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun ProfileScreen(params: ProfileScreenParams) {
    Scaffold(
        topBar = {
            RyggTopAppBar(title = stringResource(R.string.profile_title), actions = {})
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            if (params.uiState.isSignedIn) {
                SignedInContent(params)
            } else {
                GuestContent(params)
            }

            SettingsEntryRow(onOpenSettings = params.onOpenSettings)
        }
    }
}

@Composable
private fun SignedInContent(params: ProfileScreenParams) {
    RyggCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.BrandGreen),
                modifier = Modifier.size(RyggTheme.dimens.iconSize80)
            )
            Text(
                text = params.uiState.displayName.ifEmpty { stringResource(R.string.profile_no_name) },
                style = RyggTheme.typography.headlineSmall,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            if (params.uiState.email.isNotEmpty()) {
                Text(
                    text = params.uiState.email,
                    style = RyggTheme.typography.bodyMedium,
                    color = RyggTheme.getColor(RyggColor.TextSecondary)
                )
            }
            Row(
                modifier = Modifier.padding(top = RyggTheme.dimens.commonContentPadding8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.BrandGreen),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
                Text(
                    text = stringResource(R.string.profile_backup_on),
                    style = RyggTheme.typography.bodyMedium,
                    color = RyggTheme.getColor(RyggColor.BrandGreen),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    TextButton(
        onClick = params.onSignOut,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.profile_sign_out),
            color = RyggTheme.getColor(RyggColor.Error)
        )
    }
}

@Composable
private fun GuestContent(params: ProfileScreenParams) {
    RyggCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.size(RyggTheme.dimens.iconSize80)
            )
            Text(
                text = stringResource(R.string.profile_guest_title),
                style = RyggTheme.typography.headlineSmall,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.profile_guest_message),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
            RyggPrimaryButton(
                text = stringResource(R.string.profile_sign_in),
                onClick = params.onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = RyggTheme.dimens.commonContentPadding4)
            )
        }
    }
}

@Composable
private fun SettingsEntryRow(onOpenSettings: () -> Unit) {
    RyggCard(onClick = onOpenSettings) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.TextSecondary),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
                Text(
                    text = stringResource(R.string.profile_open_settings),
                    style = RyggTheme.typography.bodyLarge,
                    color = RyggTheme.getColor(RyggColor.TextPrimary)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
    }
}

data class ProfileUiState(
    val isSignedIn: Boolean = false,
    val displayName: String = "",
    val email: String = ""
)

data class ProfileScreenParams(
    val uiState: ProfileUiState,
    val onSignOut: () -> Unit,
    val onSignIn: () -> Unit,
    val onOpenSettings: () -> Unit
)
