package com.example.rygg.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimensions {
    val zeroPadding: Dp = 0.dp

    val commonContentPadding4: Dp = 4.dp
    val commonContentPadding8: Dp = 8.dp
    val commonContentPadding12: Dp = 12.dp
    val commonContentPadding16: Dp = 16.dp
    val commonContentPadding20: Dp = 20.dp
    val commonContentPadding24: Dp = 24.dp
    val commonContentPadding32: Dp = 32.dp
    val commonContentPadding40: Dp = 40.dp
    val commonContentPadding48: Dp = 48.dp
    val commonContentPadding56: Dp = 56.dp
    val commonContentPadding64: Dp = 64.dp
    val commonContentPadding80: Dp = 80.dp

    val commonSpacing4: Dp = 4.dp
    val commonSpacing8: Dp = 8.dp
    val commonSpacing12: Dp = 12.dp
    val commonSpacing16: Dp = 16.dp
    val commonSpacing20: Dp = 20.dp
    val commonSpacing24: Dp = 24.dp
    val commonSpacing32: Dp = 32.dp
    val commonSpacing40: Dp = 40.dp
    val commonSpacing48: Dp = 48.dp
    val commonSpacing64: Dp = 64.dp

    val iconSize16: Dp = 16.dp
    val iconSize24: Dp = 24.dp
    val iconSize32: Dp = 32.dp
    val iconSize40: Dp = 40.dp
    val iconSize64: Dp = 64.dp
    val iconSize80: Dp = 80.dp
    val iconSize92: Dp = 92.dp
    val iconSize100: Dp = 100.dp

    val radius4: Dp = 4.dp
    val radius8: Dp = 8.dp
    val radius12: Dp = 12.dp
    val radius16: Dp = 16.dp
    val radius24: Dp = 24.dp

    val border0: Dp = 0.dp
    val border1: Dp = 1.dp
    val border2: Dp = 2.dp

    val elevation0: Dp = 0.dp
    val elevation2: Dp = 2.dp
    val elevation4: Dp = 4.dp

    val buttonHeight50: Dp = 50.dp
    val progressIndicator20: Dp = 20.dp
    val progressIndicator24: Dp = 24.dp
}

val LocalRyggDimensions = staticCompositionLocalOf { Dimensions }
