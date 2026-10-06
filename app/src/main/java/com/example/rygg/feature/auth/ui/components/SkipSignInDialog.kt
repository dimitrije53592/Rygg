package com.example.rygg.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun SkipSignInDialog(
    onContinueAsGuest: () -> Unit,
    onSignIn: () -> Unit
) {
    Dialog(
        onDismissRequest = onSignIn,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = RyggTheme.dimens.commonContentPadding24)
                .fillMaxWidth()
                .clip(RoundedCornerShape(RyggTheme.dimens.radius24))
                .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
                .verticalScroll(rememberScrollState())
                .padding(RyggTheme.dimens.commonContentPadding24),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(RyggTheme.dimens.iconSize48)
                    .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
                    .background(RyggTheme.getColor(RyggColor.MossSurface)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.BrandGreen),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
            }
            Text(
                text = stringResource(R.string.auth_skip_title),
                style = RyggTheme.typography.headlineSmall,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.auth_skip_message),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
            ) {
                SkipDrawbackRow(text = stringResource(R.string.auth_skip_benefit_backup))
                SkipDrawbackRow(text = stringResource(R.string.auth_skip_benefit_sync))
                SkipDrawbackRow(text = stringResource(R.string.auth_skip_benefit_share))
            }
            Text(
                text = stringResource(R.string.auth_skip_footnote),
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
            RyggPrimaryButton(
                text = stringResource(R.string.auth_skip_sign_in),
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth()
            )
            RyggPrimaryButton(
                text = stringResource(R.string.auth_skip_continue),
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth(),
                textColor = RyggTheme.getColor(RyggColor.TextSecondary),
                backgroundColor = RyggTheme.getColor(RyggColor.Surface),
                borderWidth = RyggTheme.dimens.border1,
                borderColor = RyggTheme.getColor(RyggColor.Outline)
            )
        }
    }
}

@Composable
private fun SkipDrawbackRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.bulletSize6)
                .clip(CircleShape)
                .background(RyggTheme.getColor(RyggColor.BrandGreen))
        )
        Text(
            text = text,
            style = RyggTheme.typography.bodyMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun SkipSignInDialogPreview() {
    RyggTheme {
        SkipSignInDialog(onContinueAsGuest = {}, onSignIn = {})
    }
}
