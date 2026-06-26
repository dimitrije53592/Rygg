package com.example.rygg.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class RyggColor(val lightColor: Color, val darkColor: Color) {
    Brand(lightColor = Color(0xFF4A7BF6), darkColor = Color(0xFF6B9BFF)),
    BrandSecondary(lightColor = Color(0xFF8A5CF5), darkColor = Color(0xFFA47CFF)),
    OnBrand(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFF000000)),
    Background(lightColor = Color(0xFFFFFBFE), darkColor = Color(0xFF1C1B1F)),
    Surface(lightColor = Color(0xFFFFFFFF), darkColor = Color(0xFF2B2B2B)),
    TextPrimary(lightColor = Color(0xFF1C1B1F), darkColor = Color(0xFFE6E1E5)),
    TextSecondary(lightColor = Color(0xFF49454F), darkColor = Color(0xFFCAC4D0)),
    TextDisabled(lightColor = Color(0xFF9E9E9E), darkColor = Color(0xFF6B6B6B)),
    Outline(lightColor = Color(0xFFB2B2B2), darkColor = Color(0xFF6B6B6B)),
    Divider(lightColor = Color(0x1F000000), darkColor = Color(0x1FFFFFFF)),
    Success(lightColor = Color(0xFF2E7D32), darkColor = Color(0xFF81C784)),
    Warning(lightColor = Color(0xFFF59E0B), darkColor = Color(0xFFFFB74D)),
    Info(lightColor = Color(0xFF0288D1), darkColor = Color(0xFF4FC3F7)),
    Error(lightColor = Color(0xFFD32F2F), darkColor = Color(0xFFEF5350))
}

val LightColorScheme = lightColorScheme(
    primary = RyggColor.Brand.lightColor,
    onPrimary = RyggColor.OnBrand.lightColor,
    secondary = RyggColor.BrandSecondary.lightColor,
    onSecondary = RyggColor.OnBrand.lightColor,
    background = RyggColor.Background.lightColor,
    onBackground = RyggColor.TextPrimary.lightColor,
    surface = RyggColor.Surface.lightColor,
    onSurface = RyggColor.TextPrimary.lightColor,
    outline = RyggColor.Outline.lightColor,
    error = RyggColor.Error.lightColor
)

val DarkColorScheme = darkColorScheme(
    primary = RyggColor.Brand.darkColor,
    onPrimary = RyggColor.OnBrand.darkColor,
    secondary = RyggColor.BrandSecondary.darkColor,
    onSecondary = RyggColor.OnBrand.darkColor,
    background = RyggColor.Background.darkColor,
    onBackground = RyggColor.TextPrimary.darkColor,
    surface = RyggColor.Surface.darkColor,
    onSurface = RyggColor.TextPrimary.darkColor,
    outline = RyggColor.Outline.darkColor,
    error = RyggColor.Error.darkColor
)
