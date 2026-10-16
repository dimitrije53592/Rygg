package com.dimitrijeilic.rygg.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.navigation.TopLevelDestination
import com.dimitrijeilic.rygg.core.navigation.isOn
import com.dimitrijeilic.rygg.core.navigation.navigateToTab
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggElevation
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.ryggElevation
import com.dimitrijeilic.rygg.core.ui.utils.formatStopwatch

@Composable
fun RyggBottomAppBar(
    navController: NavController,
    currentDestination: NavDestination?,
    recordingElapsedMillis: Long?,
    recordingPaused: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
                .navigationBarsPadding()
        ) {
            HorizontalDivider(color = RyggTheme.getColor(RyggColor.Outline))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RyggTheme.dimens.bottomBarHeight64),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab(
                    destination = TopLevelDestination.LIBRARY,
                    currentDestination = currentDestination,
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.width(RyggTheme.dimens.fabSize72))
                NavTab(
                    destination = TopLevelDestination.MAP,
                    currentDestination = currentDestination,
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        RecordButton(
            selected = currentDestination.isOn(TopLevelDestination.RECORD),
            elapsedMillis = recordingElapsedMillis,
            isPaused = recordingPaused,
            onClick = { navController.navigateToTab(TopLevelDestination.RECORD.route) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .offset(y = -RyggTheme.dimens.fabLift8)
        )
    }
}

// While a recording runs the FAB becomes a live readout (dot + stopwatch) ringed in the accent,
// so the session is never invisible from another tab.
@Composable
private fun RecordButton(
    selected: Boolean,
    elapsedMillis: Long?,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isLive = elapsedMillis != null
    val container by animateColorAsState(
        targetValue = if (selected && !isLive) {
            RyggTheme.getColor(RyggColor.AccentBright)
        } else {
            RyggTheme.getColor(RyggColor.BrandGreen)
        },
        animationSpec = RyggMotion.effects(),
        label = "recordContainer"
    )
    val ringColor = if (isPaused) RyggTheme.getColor(RyggColor.Warning) else RyggTheme.getColor(RyggColor.AccentBright)
    val label = stringResource(TopLevelDestination.RECORD.labelRes)
    val spoken = elapsedMillis?.let {
        stringResource(R.string.nav_record_live, formatStopwatch(it))
    } ?: label

    Box(
        modifier = modifier
            .size(RyggTheme.dimens.fabSize72)
            .pressScale(interactionSource)
            .ryggElevation(level = RyggElevation.Floating, shape = CircleShape)
            .clip(CircleShape)
            .background(container)
            .then(
                if (isLive) {
                    Modifier.border(
                        width = RyggTheme.dimens.border3,
                        color = ringColor,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = spoken
                role = Role.Tab
                this.selected = selected
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isLive,
            transitionSpec = { fadeIn(RyggMotion.effects()) togetherWith fadeOut(RyggMotion.effectsFast()) },
            label = "recordFabContent"
        ) { live ->
            if (live && elapsedMillis != null) {
                LiveReadout(elapsedMillis = elapsedMillis, isPaused = isPaused, dotColor = ringColor)
            } else {
                Icon(
                    painter = painterResource(TopLevelDestination.RECORD.icon),
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.OnBrand),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize40)
                )
            }
        }
    }
}

@Composable
private fun LiveReadout(
    elapsedMillis: Long,
    isPaused: Boolean,
    dotColor: androidx.compose.ui.graphics.Color
) {
    val reduceMotion = RyggTheme.reduceMotion
    val transition = rememberInfiniteTransition(label = "fabPulse")
    val pulse by transition.animateFloat(
        initialValue = PULSE_MIN_ALPHA,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = PULSE_MILLIS),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabPulseAlpha"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing2)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.statusDotSize8)
                .graphicsLayer { alpha = if (isPaused || reduceMotion) 1f else pulse }
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = formatStopwatch(elapsedMillis),
            style = RyggTheme.textStyles.fabReadout,
            color = RyggTheme.getColor(RyggColor.OnBrand),
            maxLines = 1
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
    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "navTabIcon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.TextPrimary)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "navTabLabel"
    )
    val indicator by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = RyggMotion.spatialFast(),
        label = "navTabIndicator"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (!selected) navController.navigateToTab(destination.route) }
            )
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
            .padding(vertical = RyggTheme.dimens.commonContentPadding4),
        horizontalAlignment = Alignment.CenterHorizontally,
        // Pill (32) + gap + one label line must fit the 64dp bar, or the label clips.
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing2, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .width(RyggTheme.dimens.navIndicatorWidth)
                .height(RyggTheme.dimens.navIndicatorHeight)
                .pressScale(interactionSource),
            contentAlignment = Alignment.Center
        ) {
            // The pill grows out from the icon rather than fading in place.
            Box(
                modifier = Modifier
                    .width(RyggTheme.dimens.navIndicatorWidth)
                    .height(RyggTheme.dimens.navIndicatorHeight)
                    .graphicsLayer {
                        alpha = indicator
                        scaleX = INDICATOR_MIN_SCALE + (1f - INDICATOR_MIN_SCALE) * indicator
                    }
                    .clip(RyggShapes.navIndicator)
                    .background(RyggTheme.getColor(RyggColor.MossSurface))
            )
            Icon(
                painter = painterResource(destination.icon),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
        Text(
            text = stringResource(destination.labelRes),
            style = RyggTheme.typography.labelMedium,
            color = labelColor,
            maxLines = 1
        )
    }
}

private const val PULSE_MILLIS = 900
private const val PULSE_MIN_ALPHA = 0.35f
private const val INDICATOR_MIN_SCALE = 0.6f
