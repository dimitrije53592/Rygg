package com.example.rygg.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.rygg.core.navigation.TopLevelDestination
import com.example.rygg.core.navigation.navigateToTab
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggElevation
import com.example.rygg.core.ui.theme.RyggMotion
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ryggElevation

// Recording is the app's primary action, so it gets a raised centre button rather than being one
// flat tab of three. Library and Map sit either side of the gap it leaves.
@Composable
fun RyggBottomAppBar(
    navController: NavController,
    currentDestination: NavDestination?
) {
    val onTopLevel = TopLevelDestination.entries.any { currentDestination.isOn(it) }
    if (!onTopLevel) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = RyggTheme.dimens.fabRaise20)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
                .navigationBarsPadding()
                .height(RyggTheme.dimens.bottomBarHeight64),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab(
                destination = TopLevelDestination.LIBRARY,
                currentDestination = currentDestination,
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.width(RyggTheme.dimens.fabSize64))
            NavTab(
                destination = TopLevelDestination.MAP,
                currentDestination = currentDestination,
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }

        RecordButton(
            selected = currentDestination.isOn(TopLevelDestination.RECORD),
            onClick = { navController.navigateToTab(TopLevelDestination.RECORD.route) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun RecordButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.AccentBright)
        } else {
            RyggTheme.getColor(RyggColor.BrandGreen)
        },
        animationSpec = RyggMotion.effects(),
        label = "recordContainer"
    )

    Box(
        modifier = modifier
            .size(RyggTheme.dimens.fabSize64)
            .pressScale(interactionSource)
            .ryggElevation(level = RyggElevation.Floating, shape = CircleShape)
            .clip(CircleShape)
            .background(container)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(TopLevelDestination.RECORD.icon),
            contentDescription = stringResource(TopLevelDestination.RECORD.labelRes),
            tint = RyggTheme.getColor(RyggColor.OnBrand),
            modifier = Modifier.size(RyggTheme.dimens.iconSize32)
        )
    }
}

@Composable
private fun NavTab(
    destination: TopLevelDestination,
    currentDestination: NavDestination?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val selected = currentDestination.isOn(destination)
    val interactionSource = remember { MutableInteractionSource() }
    val content by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "navTabContent"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (!selected) navController.navigateToTab(destination.route) }
            )
            .padding(vertical = RyggTheme.dimens.commonContentPadding8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Icon(
            painter = painterResource(destination.icon),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
        Text(
            text = stringResource(destination.labelRes),
            style = RyggTheme.textStyles.trackedLabel,
            color = content
        )
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
