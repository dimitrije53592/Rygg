package com.dimitrijeilic.rygg.core

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.core.locale.AppLocaleStore
import com.dimitrijeilic.rygg.core.navigation.AppNavigation
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.ThemeMode
import com.dimitrijeilic.rygg.core.units.LocalMeasurementSystem
import com.dimitrijeilic.rygg.feature.settings.ui.viewmodel.ThemeViewModel
import com.dimitrijeilic.rygg.feature.settings.ui.viewmodel.UnitsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleStore.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val unitsViewModel: UnitsViewModel = hiltViewModel()
            val measurementSystem by unitsViewModel.measurementSystem.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // enableEdgeToEdge reads bar icon luminance from the *system* dark mode, so an app set
            // to Dark on a Light system draws dark icons on a dark bar. Re-apply from the resolved theme.
            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme }
                )
            }

            RyggTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalMeasurementSystem provides measurementSystem) {
                    AppNavigation()
                }
            }
        }
    }
}

private val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
