package com.dimitrijeilic.rygg.feature.profile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggCard
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggIconBadge
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggTopAppBar
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter

@Composable
fun ProfileScreen(params: ProfileScreenParams) {
    var confirmSignOut by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = RyggTheme.getColor(RyggColor.SurfaceDim),
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.profile_title),
                onNavigateBack = params.onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            if (params.uiState.isSignedIn) {
                SignedInHeader(params.uiState)
            } else {
                GuestHeader(onSignIn = params.onSignIn)
            }

            LifetimeStats(params.uiState)

            RyggCard(contentPadding = RyggTheme.dimens.zeroPadding) {
                LinkRow(
                    label = stringResource(R.string.profile_open_settings),
                    icon = Icons.Outlined.Settings,
                    onClick = params.onOpenSettings
                )
                RowDivider()
                LinkRow(
                    label = stringResource(R.string.profile_send_feedback),
                    icon = Icons.Outlined.Feedback,
                    onClick = params.onSendFeedback
                )
            }

            if (params.uiState.isSignedIn) {
                RyggCard(contentPadding = RyggTheme.dimens.zeroPadding) {
                    LinkRow(
                        label = stringResource(R.string.profile_sign_out),
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        onClick = { confirmSignOut = true },
                        showChevron = false
                    )
                }
            }
        }
    }

    if (confirmSignOut) {
        RyggDialog(
            title = stringResource(R.string.profile_sign_out_confirm_title),
            onDismissRequest = { confirmSignOut = false },
            icon = Icons.AutoMirrored.Outlined.Logout
        ) {
            RyggPrimaryButton(
                text = stringResource(R.string.profile_sign_out),
                onClick = {
                    confirmSignOut = false
                    params.onSignOut()
                },
                modifier = Modifier.fillMaxWidth()
            )
            RyggSecondaryButton(
                text = stringResource(R.string.profile_sign_out_cancel),
                onClick = { confirmSignOut = false },
                textColor = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SignedInHeader(uiState: ProfileUiState) {
    val name = uiState.displayName.ifEmpty { stringResource(R.string.profile_no_name) }
    RyggCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            MonogramAvatar(name = uiState.displayName.ifEmpty { uiState.email })
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing2)
            ) {
                Text(
                    text = name,
                    style = RyggTheme.typography.titleLarge,
                    color = RyggTheme.getColor(RyggColor.TextPrimary)
                )
                if (uiState.email.isNotEmpty()) {
                    Text(
                        text = uiState.email,
                        style = RyggTheme.typography.bodyMedium,
                        color = RyggTheme.getColor(RyggColor.TextSecondary)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = RyggTheme.dimens.commonSpacing4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDone,
                        contentDescription = null,
                        tint = RyggTheme.getColor(RyggColor.BrandGreen),
                        modifier = Modifier.size(RyggTheme.dimens.iconSize16)
                    )
                    Text(
                        text = stringResource(R.string.profile_backup_on),
                        style = RyggTheme.typography.labelMedium,
                        color = RyggTheme.getColor(RyggColor.BrandGreen)
                    )
                }
            }
        }
    }
}

// Initials on the brand squircle read as "this is you" far better than a stock person glyph.
@Composable
private fun MonogramAvatar(name: String) {
    val initials = name.split(' ', '.', '@')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.avatarSize)
            .clip(RyggShapes.card)
            .background(RyggTheme.getColor(RyggColor.BrandGreen)),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isEmpty()) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.OnBrand),
                modifier = Modifier.size(RyggTheme.dimens.iconSize32)
            )
        } else {
            Text(
                text = initials,
                style = RyggTheme.typography.headlineSmall,
                color = RyggTheme.getColor(RyggColor.OnBrand)
            )
        }
    }
}

@Composable
private fun GuestHeader(onSignIn: () -> Unit) {
    RyggCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            RyggIconBadge(
                icon = Icons.Outlined.AccountCircle,
                tint = RyggTheme.getColor(RyggColor.TextSecondary),
                containerColor = RyggTheme.getColor(RyggColor.Surface)
            )
            Text(
                text = stringResource(R.string.profile_guest_title),
                style = RyggTheme.typography.titleLarge,
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
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = RyggTheme.dimens.commonContentPadding4)
            )
        }
    }
}

@Composable
private fun LifetimeStats(uiState: ProfileUiState) {
    if (uiState.activityCount == 0) return
    val units = rememberUnitFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
        Text(
            text = stringResource(R.string.profile_lifetime).uppercase(),
            style = RyggTheme.textStyles.trackedLabel,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.padding(start = RyggTheme.dimens.commonContentPadding4)
        )
        RyggCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RyggStat(
                    value = uiState.activityCount.toString(),
                    label = stringResource(R.string.library_summary_activities)
                )
                RyggStat(
                    quantity = units.distanceParts(uiState.totalDistanceMeters),
                    label = stringResource(R.string.details_stat_distance)
                )
                RyggStat(
                    quantity = units.elevationParts(uiState.totalAscentMeters),
                    label = stringResource(R.string.details_stat_ascent)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.LinkRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showChevron: Boolean = true,
    labelColor: Color = RyggTheme.getColor(RyggColor.TextPrimary)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = RyggTheme.dimens.listRowHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = RyggTheme.dimens.commonContentPadding16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        RyggIconBadge(icon = icon)
        Text(
            text = label,
            style = RyggTheme.typography.bodyLarge,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        } else {
            Spacer(Modifier.size(RyggTheme.dimens.iconSize24))
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        color = RyggTheme.getColor(RyggColor.Outline),
        modifier = Modifier.padding(
            start = RyggTheme.dimens.commonContentPadding16 + RyggTheme.dimens.listDividerInset
        )
    )
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    RyggTheme {
        ProfileScreen(
            params = ProfileScreenParams(
                uiState = ProfileUiState(
                    isSignedIn = true,
                    displayName = "Ana Petrović",
                    email = "ana@example.com",
                    activityCount = 42,
                    totalDistanceMeters = 512_300.0,
                    totalAscentMeters = 31_400.0
                ),
                onNavigateBack = {},
                onSignOut = {},
                onSignIn = {},
                onOpenSettings = {},
                onSendFeedback = {}
            )
        )
    }
}

data class ProfileUiState(
    val isSignedIn: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val activityCount: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalAscentMeters: Double = 0.0
)

data class ProfileScreenParams(
    val uiState: ProfileUiState,
    val onNavigateBack: () -> Unit,
    val onSignOut: () -> Unit,
    val onSignIn: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onSendFeedback: () -> Unit
)
