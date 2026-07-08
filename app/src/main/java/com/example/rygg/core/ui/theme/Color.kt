package com.example.rygg.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class RyggColor(val lightColor: Color, val darkColor: Color) {
    BrandGreen(lightColor = Color(0xFF0D7A52), darkColor = Color(0xFF0E8257)),
    BrandDarkGreen(lightColor = Color(0xFF1D3631), darkColor = Color(0xFF1B3A31)),
    BrandGraphite(lightColor = Color(0xFF151A1F), darkColor = Color(0xFF0E1216)),
    MutedGray(lightColor = Color(0xFFA8ADA9), darkColor = Color(0xFF8A938E)),
    Surface(lightColor = Color(0xFFF5F5F5), darkColor = Color(0xFF20262B)),
    SurfaceElevated(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFF2A3138)),
    SurfaceDim(lightColor = Color(0xFFF2F2F2), darkColor = Color(0xFF121417)),
    OnBrand(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFFF5F7F6)),
    TextPrimary(lightColor = Color(0xFF1A1F26), darkColor = Color(0xFFEAEEEC)),
    TextSecondary(lightColor = Color(0xFF6D737A), darkColor = Color(0xFF9BA5A0)),
    Outline(lightColor = Color(0xFFE6F2ED), darkColor = Color(0xFF2E3A36)),
    Success(lightColor = Color(0xFF2E7D32), darkColor = Color(0xFF81C784)),
    Error(lightColor = Color(0xFFD32F2F), darkColor = Color(0xFFEF5350))
}

val LightColorScheme = lightColorScheme(
    primary = RyggColor.BrandGreen.lightColor,
    onPrimary = RyggColor.MutedGray.lightColor,
    secondary = RyggColor.BrandGraphite.lightColor,
    onSecondary = RyggColor.MutedGray.lightColor,
    background = RyggColor.SurfaceDim.lightColor,
    onBackground = RyggColor.TextPrimary.lightColor,
    surface = RyggColor.Surface.lightColor,
    onSurface = RyggColor.TextPrimary.lightColor,
    outline = RyggColor.Outline.lightColor,
    error = RyggColor.Error.lightColor
)

val DarkColorScheme = darkColorScheme(
    primary = RyggColor.BrandGreen.darkColor,
    onPrimary = RyggColor.MutedGray.darkColor,
    secondary = RyggColor.BrandGraphite.darkColor,
    onSecondary = RyggColor.MutedGray.darkColor,
    background = RyggColor.SurfaceDim.darkColor,
    onBackground = RyggColor.TextPrimary.darkColor,
    surface = RyggColor.Surface.darkColor,
    onSurface = RyggColor.TextPrimary.darkColor,
    outline = RyggColor.Outline.darkColor,
    error = RyggColor.Error.darkColor
)
