package com.example.rygg.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

/**
 * Shared shell for the auth screens (login / register / forgot password).
 *
 * Renders the graphite backdrop, the brand hero (logo + wordmark) and a rounded
 * card that hosts the form passed via [content]. The card fills the remaining
 * height to the bottom edge and keeps its form contents vertically centered.
 */
@Composable
fun AuthScaffold(
    modifier: Modifier = Modifier,
    showSkip: Boolean = false,
    onSkipClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RyggTheme.getColor(RyggColor.BrandGraphite)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RyggTheme.dimens.commonContentPadding56)
                .padding(top = RyggTheme.dimens.commonContentPadding16),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSkip) {
                Text(
                    text = stringResource(R.string.login_skip),
                    style = RyggTheme.typography.labelLarge,
                    color = RyggTheme.getColor(RyggColor.OnBrand),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RyggTheme.getColor(RyggColor.OnBrand).copy(alpha = 0.12f))
                        .clickable { onSkipClick() }
                        .padding(
                            horizontal = RyggTheme.dimens.commonContentPadding16,
                            vertical = RyggTheme.dimens.commonContentPadding8
                        )
                )
            }
        }
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing24))
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
                contentDescription = null,
                modifier = Modifier.size(RyggTheme.dimens.iconSize80)
            )
        }
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing8))
        Text(
            text = stringResource(R.string.app_name),
            color = RyggTheme.getColor(RyggColor.OnBrand),
            style = RyggTheme.typography.headlineLarge
        )
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing32))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(RyggTheme.dimens.radius24))
                .background(RyggTheme.getColor(RyggColor.Background))
                .padding(RyggTheme.dimens.commonContentPadding24),
            verticalArrangement = Arrangement.spacedBy(
                RyggTheme.dimens.commonSpacing16,
                Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
