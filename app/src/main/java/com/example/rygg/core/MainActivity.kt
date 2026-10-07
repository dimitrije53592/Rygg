package com.example.rygg.core

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.core.locale.AppLocaleStore
import com.example.rygg.core.navigation.AppNavigation
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ThemeMode
import com.example.rygg.feature.settings.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

// Scrims the system draws behind the navigation bar on devices that need one (gesture navigation
// gets none). Values follow the platform's own edge-to-edge sample.
private val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

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

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // enableEdgeToEdge decides bar icon luminance from the *system* dark mode, so an app set
            // to Dark on a Light system renders dark icons on a dark bar. Re-apply from the theme the
            // app actually resolved.
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
                AppNavigation()
            }
        }
    }
}
