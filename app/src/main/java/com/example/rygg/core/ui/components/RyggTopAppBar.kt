package com.example.rygg.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RyggTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The back control already owns the leading slot.
                if (onNavigateBack == null) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logo),
                        tint = RyggTheme.getColor(RyggColor.BrandGreen),
                        modifier = Modifier.size(RyggTheme.dimens.iconSize32),
                        contentDescription = null
                    )
                }
                Text(
                    text = title,
                    style = RyggTheme.typography.titleLarge,
                    color = RyggTheme.getColor(RyggColor.OnBrand),
                    modifier = Modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding8)
                )
            }
        },
        navigationIcon = {
            onNavigateBack?.let { navigateBack ->
                RyggTopBarAction(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back),
                    onClick = navigateBack,
                    modifier = Modifier.padding(start = RyggTheme.dimens.commonContentPadding8)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
            .background(RyggTheme.getColor(RyggColor.BrandGraphite))
            .headerContourLines(RyggTheme.getColor(RyggColor.OnBrand)),
        actions = actions
    )
}
