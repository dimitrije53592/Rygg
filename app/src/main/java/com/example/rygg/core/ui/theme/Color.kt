package com.example.rygg.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class RyggColor(val lightColor: Color, val darkColor: Color) {
    BrandGreen(lightColor = Color(0xFF0D7A52), darkColor = Color(0xFF6B9BFF)),
    BrandGraphite(lightColor = Color(0xFF151A1F), darkColor = Color(0xFFA47CFF)),
    OnBrandLightGray(lightColor = Color(0xFFA8ADA9), darkColor = Color(0xFF000000)),
    BackgroundWhite(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFF1C1B1F)),
    SurfaceGray(lightColor = Color(0xFFF5F5F5), darkColor = Color(0xFF2B2B2B)),
    TextPrimary(lightColor = Color(0xFF1A1F26), darkColor = Color(0xFFE6E1E5)),
    TextSecondary(lightColor = Color(0xFF6D737A), darkColor = Color(0xFFCAC4D0)),
    Outline(lightColor = Color(0xFFE6F2ED), darkColor = Color(0xFF6B6B6B)),
    Success(lightColor = Color(0xFF2E7D32), darkColor = Color(0xFF81C784)),
    Error(lightColor = Color(0xFFD32F2F), darkColor = Color(0xFFEF5350))
}

val LightColorScheme = lightColorScheme(
    primary = RyggColor.BrandGreen.lightColor,
    onPrimary = RyggColor.OnBrandLightGray.lightColor,
    secondary = RyggColor.BrandGraphite.lightColor,
    onSecondary = RyggColor.OnBrandLightGray.lightColor,
    background = RyggColor.BackgroundWhite.lightColor,
    onBackground = RyggColor.TextPrimary.lightColor,
    surface = RyggColor.SurfaceGray.lightColor,
    onSurface = RyggColor.TextPrimary.lightColor,
    outline = RyggColor.Outline.lightColor,
    error = RyggColor.Error.lightColor
)

val DarkColorScheme = darkColorScheme(
    primary = RyggColor.BrandGreen.darkColor,
    onPrimary = RyggColor.OnBrandLightGray.darkColor,
    secondary = RyggColor.BrandGraphite.darkColor,
    onSecondary = RyggColor.OnBrandLightGray.darkColor,
    background = RyggColor.BackgroundWhite.darkColor,
    onBackground = RyggColor.TextPrimary.darkColor,
    surface = RyggColor.SurfaceGray.darkColor,
    onSurface = RyggColor.TextPrimary.darkColor,
    outline = RyggColor.Outline.darkColor,
    error = RyggColor.Error.darkColor
)
