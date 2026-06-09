package de.piecha.switchwerk

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.piecha.switchwerk.ui.screens.SettingsScreen
import de.piecha.switchwerk.ui.screens.StartScreen
import de.piecha.switchwerk.ui.theme.SwitchWerkTheme

private enum class AppScreen {
    Dashboard,
    Settings
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        setContent {
            SwitchWerkTheme {
                SwitchWerkAppContent()
            }
        }
    }
}

@Composable
private fun SwitchWerkAppContent() {
    var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }

    when (currentScreen) {
        AppScreen.Dashboard -> StartScreen(
            onNavigateToSettings = {
                currentScreen = AppScreen.Settings
            }
        )

        AppScreen.Settings -> SettingsScreen(
            onNavigateBack = {
                currentScreen = AppScreen.Dashboard
            }
        )
    }
}
