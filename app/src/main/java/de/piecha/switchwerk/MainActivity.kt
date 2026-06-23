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
import androidx.compose.runtime.SideEffect
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
import de.piecha.switchwerk.ui.screens.SettingsScreenUiState
import de.piecha.switchwerk.ui.screens.StartScreen
import de.piecha.switchwerk.ui.screens.HelpScreen
import de.piecha.switchwerk.ui.screens.AboutScreen
import de.piecha.switchwerk.ui.theme.SwitchWerkTheme
import de.piecha.switchwerk.ui.AppLocaleController
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.viewmodel.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.compose.viewmodel.koinViewModel

private enum class AppScreen {
    Dashboard,
    Settings,
    Help,
    About
}

private const val STATE_CURRENT_SCREEN = "current_screen"
private const val STATE_HELP_RETURN_SCREEN = "help_return_screen"
private const val STATE_SETTINGS_SECTION = "settings_section"
private const val STATE_PENDING_FILE_IMPORT_MODE = "pending_file_import_mode"
private const val STATE_PENDING_QR_IMPORT_MODE = "pending_qr_import_mode"
private const val STATE_FILE_IMPORT_MODE = "file_import_mode"
private const val STATE_QR_IMPORT_MODE = "qr_import_mode"
private const val STATE_URL_IMPORT_MODE = "url_import_mode"
private const val STATE_URL_IMPORT_VALUE = "url_import_value"
private const val STATE_SHOW_FILE_IMPORT_MODE_DIALOG = "show_file_import_mode_dialog"
private const val STATE_SHOW_QR_IMPORT_MODE_DIALOG = "show_qr_import_mode_dialog"
private const val STATE_SHOW_URL_IMPORT_DIALOG = "show_url_import_dialog"
private const val STATE_SHOW_PASSWORD_EXPORT_WARNING = "show_password_export_warning"
private const val STATE_OPEN_SWIPE_ITEM_ID = "open_swipe_item_id"
private const val STATE_PENDING_DELETE_PROFILE_ID = "pending_delete_profile_id"

class MainActivity : ComponentActivity() {
    private val appSettingsRepository: AppSettingsRepository by inject()
    private var currentScreenForRestoration = AppScreen.Dashboard
    private var helpReturnScreenForRestoration = AppScreen.Dashboard
    private var selectedSettingsSectionForRestoration = SettingsSection.WIFI_PROFILES
    private var settingsScreenUiStateForRestoration = SettingsScreenUiState()

    override fun onCreate(savedInstanceState: Bundle?) {
        currentScreenForRestoration = savedInstanceState.restoreAppScreen(
            STATE_CURRENT_SCREEN,
            AppScreen.Dashboard
        )
        helpReturnScreenForRestoration = savedInstanceState.restoreAppScreen(
            STATE_HELP_RETURN_SCREEN,
            AppScreen.Dashboard
        )
        selectedSettingsSectionForRestoration = savedInstanceState.restoreSettingsSection(
            STATE_SETTINGS_SECTION,
            SettingsSection.WIFI_PROFILES
        )
        settingsScreenUiStateForRestoration = savedInstanceState.restoreSettingsScreenUiState()
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
                    SwitchWerkAppContent(
                        mainViewModel = mainViewModel,
                        initialCurrentScreen = currentScreenForRestoration,
                        initialHelpReturnScreen = helpReturnScreenForRestoration,
                        initialSettingsSection = selectedSettingsSectionForRestoration,
                        initialSettingsScreenUiState = settingsScreenUiStateForRestoration,
                        onNavigationStateChanged = { currentScreen, helpReturnScreen, settingsSection ->
                            currentScreenForRestoration = currentScreen
                            helpReturnScreenForRestoration = helpReturnScreen
                            selectedSettingsSectionForRestoration = settingsSection
                        },
                        onSettingsScreenUiStateChanged = { settingsScreenUiState ->
                            settingsScreenUiStateForRestoration = settingsScreenUiState
                        }
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_CURRENT_SCREEN, currentScreenForRestoration.name)
        outState.putString(STATE_HELP_RETURN_SCREEN, helpReturnScreenForRestoration.name)
        outState.putString(STATE_SETTINGS_SECTION, selectedSettingsSectionForRestoration.name)
        outState.putString(STATE_PENDING_FILE_IMPORT_MODE, settingsScreenUiStateForRestoration.pendingFileImportMode.name)
        outState.putString(STATE_PENDING_QR_IMPORT_MODE, settingsScreenUiStateForRestoration.pendingQrImportMode.name)
        outState.putString(STATE_FILE_IMPORT_MODE, settingsScreenUiStateForRestoration.fileImportMode.name)
        outState.putString(STATE_QR_IMPORT_MODE, settingsScreenUiStateForRestoration.qrImportMode.name)
        outState.putString(STATE_URL_IMPORT_MODE, settingsScreenUiStateForRestoration.urlImportMode.name)
        outState.putString(STATE_URL_IMPORT_VALUE, settingsScreenUiStateForRestoration.urlImportValue)
        outState.putBoolean(STATE_SHOW_FILE_IMPORT_MODE_DIALOG, settingsScreenUiStateForRestoration.showFileImportModeDialog)
        outState.putBoolean(STATE_SHOW_QR_IMPORT_MODE_DIALOG, settingsScreenUiStateForRestoration.showQrImportModeDialog)
        outState.putBoolean(STATE_SHOW_URL_IMPORT_DIALOG, settingsScreenUiStateForRestoration.showUrlImportDialog)
        outState.putBoolean(STATE_SHOW_PASSWORD_EXPORT_WARNING, settingsScreenUiStateForRestoration.showPasswordExportWarning)
        outState.putString(STATE_OPEN_SWIPE_ITEM_ID, settingsScreenUiStateForRestoration.openSwipeItemId)
        outState.putString(STATE_PENDING_DELETE_PROFILE_ID, settingsScreenUiStateForRestoration.pendingDeleteProfileId)
        super.onSaveInstanceState(outState)
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

private fun Bundle?.restoreAppScreen(key: String, fallback: AppScreen): AppScreen {
    val name = this?.getString(key) ?: return fallback
    return AppScreen.entries.firstOrNull { it.name == name } ?: fallback
}

private fun Bundle?.restoreSettingsSection(key: String, fallback: SettingsSection): SettingsSection {
    val name = this?.getString(key) ?: return fallback
    return SettingsSection.entries.firstOrNull { it.name == name } ?: fallback
}

private fun Bundle?.restoreSettingsScreenUiState(): SettingsScreenUiState {
    return SettingsScreenUiState(
        pendingFileImportMode = restoreImportMode(STATE_PENDING_FILE_IMPORT_MODE),
        pendingQrImportMode = restoreImportMode(STATE_PENDING_QR_IMPORT_MODE),
        fileImportMode = restoreImportMode(STATE_FILE_IMPORT_MODE),
        qrImportMode = restoreImportMode(STATE_QR_IMPORT_MODE),
        urlImportMode = restoreImportMode(STATE_URL_IMPORT_MODE),
        urlImportValue = this?.getString(STATE_URL_IMPORT_VALUE).orEmpty(),
        showFileImportModeDialog = this?.getBoolean(STATE_SHOW_FILE_IMPORT_MODE_DIALOG) ?: false,
        showQrImportModeDialog = this?.getBoolean(STATE_SHOW_QR_IMPORT_MODE_DIALOG) ?: false,
        showUrlImportDialog = this?.getBoolean(STATE_SHOW_URL_IMPORT_DIALOG) ?: false,
        showPasswordExportWarning = this?.getBoolean(STATE_SHOW_PASSWORD_EXPORT_WARNING) ?: false,
        openSwipeItemId = this?.getString(STATE_OPEN_SWIPE_ITEM_ID),
        pendingDeleteProfileId = this?.getString(STATE_PENDING_DELETE_PROFILE_ID)
    )
}

private fun Bundle?.restoreImportMode(key: String) =
    this?.getString(key)?.let { name ->
        de.piecha.switchwerk.data.repository.ConfigurationImportMode.entries.firstOrNull {
            it.name == name
        }
    } ?: de.piecha.switchwerk.data.repository.ConfigurationImportMode.MERGE

@Composable
private fun SwitchWerkAppContent(
    mainViewModel: MainViewModel,
    initialCurrentScreen: AppScreen,
    initialHelpReturnScreen: AppScreen,
    initialSettingsSection: SettingsSection,
    initialSettingsScreenUiState: SettingsScreenUiState,
    onNavigationStateChanged: (AppScreen, AppScreen, SettingsSection) -> Unit,
    onSettingsScreenUiStateChanged: (SettingsScreenUiState) -> Unit
) {
    var currentScreen by remember { mutableStateOf(initialCurrentScreen) }
    var helpReturnScreen by remember { mutableStateOf(initialHelpReturnScreen) }
    var selectedSettingsSection by remember {
        mutableStateOf(initialSettingsSection)
    }

    SideEffect {
        onNavigationStateChanged(currentScreen, helpReturnScreen, selectedSettingsSection)
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
            },
            onNavigateToAbout = {
                currentScreen = AppScreen.About
            }
        )

        AppScreen.Settings -> SettingsScreen(
            selectedSection = selectedSettingsSection,
            onSectionSelected = { selectedSettingsSection = it },
            initialUiState = initialSettingsScreenUiState,
            onUiStateChanged = onSettingsScreenUiStateChanged,
            onNavigateBack = {
                currentScreen = AppScreen.Dashboard
            }
        )

        AppScreen.Help -> HelpScreen(
            onNavigateBack = {
                currentScreen = helpReturnScreen
            }
        )

        AppScreen.About -> AboutScreen(
            onNavigateBack = { currentScreen = AppScreen.Dashboard }
        )
    }
}
