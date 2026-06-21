package de.piecha.switchwerk

import android.graphics.Color
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.piecha.switchwerk.ui.screens.SettingsScreen
import de.piecha.switchwerk.ui.screens.SettingsSection
import de.piecha.switchwerk.ui.screens.StartScreen
import de.piecha.switchwerk.ui.screens.HelpScreen
import de.piecha.switchwerk.ui.theme.SwitchWerkTheme
import de.piecha.switchwerk.ui.AppLocaleController
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.viewmodel.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.compose.viewmodel.koinViewModel

private enum class AppScreen {
    Dashboard,
    Settings,
    Help
}

class MainActivity : ComponentActivity() {
    private val appSettingsRepository: AppSettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val initialLanguage = appSettingsRepository.settings.value.language
        AppLocaleController.apply(this, initialLanguage)
        super.onCreate(savedInstanceState)
        requestNearbyWifiPermission()
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
            val mainViewModel: MainViewModel = koinViewModel()
            val uiState by mainViewModel.uiState.collectAsState()
            LaunchedEffect(uiState.appSettings.language) {
                val language = uiState.appSettings.language
                if (language != initialLanguage && AppLocaleController.apply(this@MainActivity, language)) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        recreate()
                    }
                }
            }
            SwitchWerkTheme(themeMode = uiState.appSettings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwitchWerkAppContent(mainViewModel)
                }
            }
        }
    }

    private fun requestNearbyWifiPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES),
                NEARBY_WIFI_PERMISSION_REQUEST
            )
        }
    }

    private companion object {
        const val NEARBY_WIFI_PERMISSION_REQUEST = 1001
    }
}

@Composable
private fun SwitchWerkAppContent(mainViewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }
    var helpReturnScreen by remember { mutableStateOf(AppScreen.Dashboard) }
    var selectedSettingsSection by remember {
        mutableStateOf(SettingsSection.WIFI_PROFILES)
    }

    when (currentScreen) {
        AppScreen.Dashboard -> StartScreen(
            viewModel = mainViewModel,
            onNavigateToSettings = {
                currentScreen = AppScreen.Settings
            },
            onNavigateToHelp = {
                helpReturnScreen = AppScreen.Dashboard
                currentScreen = AppScreen.Help
            }
        )

        AppScreen.Settings -> SettingsScreen(
            selectedSection = selectedSettingsSection,
            onSectionSelected = { selectedSettingsSection = it },
            onNavigateBack = {
                currentScreen = AppScreen.Dashboard
            },
            onNavigateToHelp = {
                helpReturnScreen = AppScreen.Settings
                currentScreen = AppScreen.Help
            }
        )

        AppScreen.Help -> HelpScreen(
            onNavigateBack = {
                currentScreen = helpReturnScreen
            }
        )
    }
}
