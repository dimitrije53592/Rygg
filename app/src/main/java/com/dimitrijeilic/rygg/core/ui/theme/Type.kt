package com.dimitrijeilic.rygg.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.dimitrijeilic.rygg.R

// Bundled rather than downloadable: the app has to render identically with no network.
private val DefaultFontFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

// Font padding off and centred line boxes, so a glyph sits exactly where an icon beside it does.
private fun ryggStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp,
    tabular: Boolean = false
): TextStyle = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    fontFeatureSettings = if (tabular) TABULAR_FIGURES else null,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

// Manrope is already wide and open, so body tracking sits near zero instead of Material's
// Roboto-tuned +0.25..0.5sp; large sizes tighten so headlines hold together.
val RyggTypography = Typography(
    displayLarge = ryggStyle(FontWeight.ExtraBold, 57.sp, 64.sp, (-1.5).sp, tabular = true),
    displayMedium = ryggStyle(FontWeight.ExtraBold, 44.sp, 52.sp, (-1).sp, tabular = true),
    displaySmall = ryggStyle(FontWeight.Bold, 36.sp, 44.sp, (-0.75).sp, tabular = true),
    headlineLarge = ryggStyle(FontWeight.Bold, 32.sp, 40.sp, (-0.5).sp),
    headlineMedium = ryggStyle(FontWeight.Bold, 28.sp, 36.sp, (-0.4).sp),
    headlineSmall = ryggStyle(FontWeight.Bold, 24.sp, 32.sp, (-0.25).sp),
    titleLarge = ryggStyle(FontWeight.Bold, 20.sp, 28.sp, (-0.1).sp),
    titleMedium = ryggStyle(FontWeight.SemiBold, 16.sp, 24.sp),
    titleSmall = ryggStyle(FontWeight.SemiBold, 14.sp, 20.sp),
    bodyLarge = ryggStyle(FontWeight.Normal, 16.sp, 24.sp),
    bodyMedium = ryggStyle(FontWeight.Normal, 14.sp, 20.sp),
    bodySmall = ryggStyle(FontWeight.Normal, 12.sp, 16.sp, 0.1.sp),
    labelLarge = ryggStyle(FontWeight.SemiBold, 14.sp, 20.sp),
    labelMedium = ryggStyle(FontWeight.SemiBold, 12.sp, 16.sp, 0.1.sp),
    labelSmall = ryggStyle(FontWeight.SemiBold, 11.sp, 16.sp, 0.2.sp)
)

object RyggTextStyles {
    // Pair with .uppercase() at the call site, so the string resource stays translatable.
    val trackedLabel: TextStyle = ryggStyle(FontWeight.Bold, 11.sp, 16.sp, 0.8.sp)

    // Stopwatch inside the 72dp Record FAB; has to fit h:mm:ss.
    val fabReadout: TextStyle = ryggStyle(FontWeight.ExtraBold, 13.sp, 16.sp, (-0.2).sp, tabular = true)

    val statValueSmall: TextStyle = ryggStyle(FontWeight.Bold, 15.sp, 20.sp, tabular = true)

    val statValue: TextStyle = ryggStyle(FontWeight.Bold, 20.sp, 24.sp, (-0.2).sp, tabular = true)

    val statValueLarge: TextStyle =
        ryggStyle(FontWeight.ExtraBold, 28.sp, 32.sp, (-0.6).sp, tabular = true)

    val statValueHero: TextStyle =
        ryggStyle(FontWeight.ExtraBold, 48.sp, 52.sp, (-1.5).sp, tabular = true)
}

val LocalRyggTextStyles = staticCompositionLocalOf { RyggTextStyles }

// Keeps digit columns aligned while a stat ticks: without it a 1 is narrower than a 7.
private const val TABULAR_FIGURES = "tnum"
