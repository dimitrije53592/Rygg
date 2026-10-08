package com.example.rygg.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// The dark ramp steps SurfaceDim / Surface / SurfaceElevated by lightness, which is what carries
// depth where shadows are invisible.
enum class RyggColor(val lightColor: Color, val darkColor: Color) {
    BrandGreen(lightColor = Color(0xFF0E7A52), darkColor = Color(0xFF16A06B)),

    AccentBright(lightColor = Color(0xFF1FB87E), darkColor = Color(0xFF2FD694)),
    BrandDarkGreen(lightColor = Color(0xFF1D3631), darkColor = Color(0xFF11231D)),
    BrandGraphite(lightColor = Color(0xFF151A1F), darkColor = Color(0xFF0A0E12)),
    MutedGray(lightColor = Color(0xFFA8ADA9), darkColor = Color(0xFF6E7A85)),
    Surface(lightColor = Color(0xFFF5F5F5), darkColor = Color(0xFF141A20)),
    SurfaceElevated(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFF1C242B)),
    SurfaceDim(lightColor = Color(0xFFF2F3F2), darkColor = Color(0xFF0D1115)),
    MossSurface(lightColor = Color(0xFFE6F4EE), darkColor = Color(0xFF15302A)),
    MossSurfaceDim(lightColor = Color(0xFFD2EBDF), darkColor = Color(0xFF0E201C)),
    OnBrand(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFFF2F6F9)),
    TextPrimary(lightColor = Color(0xFF1B2026), darkColor = Color(0xFFE8EDF2)),
    TextSecondary(lightColor = Color(0xFF6B727A), darkColor = Color(0xFF94A1AD)),
    Outline(lightColor = Color(0xFFE6E8E7), darkColor = Color(0xFF2A343D)),

    // Content laid over map tiles or route imagery, so it does not flip with the theme.
    ScrimDark(lightColor = Color(0xFF000000), darkColor = Color(0xFF000000)),
    OnScrim(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFFFFFFFF)),
    Success(lightColor = Color(0xFF2E7D32), darkColor = Color(0xFF81C784)),

    Warning(lightColor = Color(0xFFB26A00), darkColor = Color(0xFFFFB74D)),

    // Elevation-profile grade bands; gentler than 3% stays the brand accent (see ElevationProfile).
    GradeModerate(lightColor = Color(0xFFCCA32E), darkColor = Color(0xFFE8C547)),
    GradeSteep(lightColor = Color(0xFFCF7429), darkColor = Color(0xFFE8893D)),
    GradeVerySteep(lightColor = Color(0xFFC03A33), darkColor = Color(0xFFE05950)),
    GradeExtreme(lightColor = Color(0xFF8E2B24), darkColor = Color(0xFFB3463D)),
    Error(lightColor = Color(0xFFD32F2F), darkColor = Color(0xFFFF6B6B)),
    ErrorSurface(lightColor = Color(0xFFFDECEA), darkColor = Color(0xFF3A1E1E))
}

// Map every Material role: an unmapped one falls back to baseline purple on stock widgets.
val LightColorScheme = lightColorScheme(
    primary = RyggColor.BrandGreen.lightColor,
    onPrimary = RyggColor.OnBrand.lightColor,
    primaryContainer = RyggColor.MossSurface.lightColor,
    onPrimaryContainer = RyggColor.BrandDarkGreen.lightColor,
    secondary = RyggColor.BrandGraphite.lightColor,
    onSecondary = RyggColor.OnBrand.lightColor,
    secondaryContainer = RyggColor.Surface.lightColor,
    onSecondaryContainer = RyggColor.TextPrimary.lightColor,
    tertiary = RyggColor.AccentBright.lightColor,
    onTertiary = RyggColor.OnBrand.lightColor,
    tertiaryContainer = RyggColor.MossSurfaceDim.lightColor,
    onTertiaryContainer = RyggColor.BrandDarkGreen.lightColor,
    background = RyggColor.SurfaceDim.lightColor,
    onBackground = RyggColor.TextPrimary.lightColor,
    surface = RyggColor.Surface.lightColor,
    onSurface = RyggColor.TextPrimary.lightColor,
    surfaceVariant = RyggColor.MossSurface.lightColor,
    onSurfaceVariant = RyggColor.TextSecondary.lightColor,
    surfaceContainerLowest = RyggColor.SurfaceElevated.lightColor,
    surfaceContainerLow = RyggColor.SurfaceElevated.lightColor,
    surfaceContainer = RyggColor.Surface.lightColor,
    surfaceContainerHigh = RyggColor.SurfaceDim.lightColor,
    surfaceContainerHighest = RyggColor.SurfaceDim.lightColor,
    outline = RyggColor.Outline.lightColor,
    outlineVariant = RyggColor.Outline.lightColor,
    scrim = RyggColor.ScrimDark.lightColor,
    inverseSurface = RyggColor.BrandGraphite.lightColor,
    inverseOnSurface = RyggColor.OnBrand.lightColor,
    inversePrimary = RyggColor.AccentBright.lightColor,
    error = RyggColor.Error.lightColor,
    onError = RyggColor.OnBrand.lightColor,
    errorContainer = RyggColor.ErrorSurface.lightColor,
    onErrorContainer = RyggColor.Error.lightColor
)

val DarkColorScheme = darkColorScheme(
    primary = RyggColor.BrandGreen.darkColor,
    onPrimary = RyggColor.OnBrand.darkColor,
    primaryContainer = RyggColor.MossSurface.darkColor,
    onPrimaryContainer = RyggColor.OnBrand.darkColor,
    secondary = RyggColor.BrandGraphite.darkColor,
    onSecondary = RyggColor.OnBrand.darkColor,
    secondaryContainer = RyggColor.Surface.darkColor,
    onSecondaryContainer = RyggColor.TextPrimary.darkColor,
    tertiary = RyggColor.AccentBright.darkColor,
    onTertiary = RyggColor.BrandGraphite.darkColor,
    tertiaryContainer = RyggColor.MossSurfaceDim.darkColor,
    onTertiaryContainer = RyggColor.OnBrand.darkColor,
    background = RyggColor.SurfaceDim.darkColor,
    onBackground = RyggColor.TextPrimary.darkColor,
    surface = RyggColor.Surface.darkColor,
    onSurface = RyggColor.TextPrimary.darkColor,
    surfaceVariant = RyggColor.MossSurface.darkColor,
    onSurfaceVariant = RyggColor.TextSecondary.darkColor,
    surfaceContainerLowest = RyggColor.SurfaceDim.darkColor,
    surfaceContainerLow = RyggColor.Surface.darkColor,
    surfaceContainer = RyggColor.Surface.darkColor,
    surfaceContainerHigh = RyggColor.SurfaceElevated.darkColor,
    surfaceContainerHighest = RyggColor.SurfaceElevated.darkColor,
    outline = RyggColor.Outline.darkColor,
    outlineVariant = RyggColor.Outline.darkColor,
    scrim = RyggColor.ScrimDark.darkColor,
    inverseSurface = RyggColor.OnBrand.darkColor,
    inverseOnSurface = RyggColor.BrandGraphite.darkColor,
    inversePrimary = RyggColor.BrandGreen.darkColor,
    error = RyggColor.Error.darkColor,
    onError = RyggColor.BrandGraphite.darkColor,
    errorContainer = RyggColor.ErrorSurface.darkColor,
    onErrorContainer = RyggColor.Error.darkColor
)
