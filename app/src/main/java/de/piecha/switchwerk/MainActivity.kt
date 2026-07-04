package de.piecha.switchwerk

import android.graphics.Color
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.piecha.switchwerk.ui.screens.SettingsScreen
import de.piecha.switchwerk.ui.screens.SettingsSection
import de.piecha.switchwerk.ui.screens.SettingsScreenUiState
import de.piecha.switchwerk.ui.screens.ImportSource
import de.piecha.switchwerk.ui.screens.StartScreen
import de.piecha.switchwerk.ui.screens.HelpScreen
import de.piecha.switchwerk.ui.screens.AboutScreen
import de.piecha.switchwerk.ui.components.SetupWizard
import de.piecha.switchwerk.ui.theme.SwitchWerkTheme
import de.piecha.switchwerk.ui.AppLocaleController
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.viewmodel.MainViewModel
import de.piecha.switchwerk.viewmodel.MainUiEvent
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
private const val STATE_IMPORT_MODE = "import_mode"
private const val STATE_URL_IMPORT_VALUE = "url_import_value"
private const val STATE_FILE_IMPORT_URI = "file_import_uri"
private const val STATE_FILE_IMPORT_REFERENCE = "file_import_reference"
private const val STATE_SHOW_IMPORT_CONFIGURATION_DIALOG = "show_import_configuration_dialog"
private const val STATE_IMPORT_SOURCE = "import_source"
private const val STATE_IMPORT_PASSWORD_CHOICE = "import_password_choice"
private const val STATE_SHOW_PASSWORD_EXPORT_WARNING = "show_password_export_warning"
private const val STATE_EXPORT_PASSWORD_CHOICE = "export_password_choice"
private const val STATE_OPEN_SWIPE_ITEM_ID = "open_swipe_item_id"

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
        outState.putString(STATE_IMPORT_MODE, settingsScreenUiStateForRestoration.importMode.name)
        outState.putString(STATE_URL_IMPORT_VALUE, settingsScreenUiStateForRestoration.urlImportValue)
        outState.putString(STATE_FILE_IMPORT_URI, settingsScreenUiStateForRestoration.fileImportUri)
        outState.putString(STATE_FILE_IMPORT_REFERENCE, settingsScreenUiStateForRestoration.fileImportReference)
        outState.putBoolean(STATE_SHOW_IMPORT_CONFIGURATION_DIALOG, settingsScreenUiStateForRestoration.showImportConfigurationDialog)
        outState.putString(STATE_IMPORT_SOURCE, settingsScreenUiStateForRestoration.importSource?.name)
        outState.putString(STATE_IMPORT_PASSWORD_CHOICE, settingsScreenUiStateForRestoration.importPasswordChoice.name)
        outState.putBoolean(STATE_SHOW_PASSWORD_EXPORT_WARNING, settingsScreenUiStateForRestoration.showPasswordExportWarning)
        outState.putString(STATE_EXPORT_PASSWORD_CHOICE, settingsScreenUiStateForRestoration.exportPasswordChoice.name)
        outState.putString(STATE_OPEN_SWIPE_ITEM_ID, settingsScreenUiStateForRestoration.openSwipeItemId)
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
        importMode = restoreImportMode(STATE_IMPORT_MODE),
        urlImportValue = this?.getString(STATE_URL_IMPORT_VALUE).orEmpty(),
        fileImportUri = this?.getString(STATE_FILE_IMPORT_URI).orEmpty(),
        fileImportReference = this?.getString(STATE_FILE_IMPORT_REFERENCE).orEmpty(),
        showImportConfigurationDialog = this?.getBoolean(STATE_SHOW_IMPORT_CONFIGURATION_DIALOG) ?: false,
        importSource = this?.getString(STATE_IMPORT_SOURCE)?.let { name ->
            ImportSource.entries.firstOrNull { it.name == name }
        },
        importPasswordChoice = this?.getString(STATE_IMPORT_PASSWORD_CHOICE)?.let { name ->
            de.piecha.switchwerk.ui.screens.PasswordTransferChoice.entries.firstOrNull { it.name == name }
        } ?: de.piecha.switchwerk.ui.screens.PasswordTransferChoice.UNDECIDED,
        showPasswordExportWarning = this?.getBoolean(STATE_SHOW_PASSWORD_EXPORT_WARNING) ?: false,
        exportPasswordChoice = this?.getString(STATE_EXPORT_PASSWORD_CHOICE)?.let { name ->
            de.piecha.switchwerk.ui.screens.PasswordTransferChoice.entries.firstOrNull { it.name == name }
        } ?: de.piecha.switchwerk.ui.screens.PasswordTransferChoice.UNDECIDED,
        openSwipeItemId = this?.getString(STATE_OPEN_SWIPE_ITEM_ID)
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
    var pendingAndroidWifiSsid by remember { mutableStateOf<String?>(null) }
    var setupWizardVisible by rememberSaveable { mutableStateOf(false) }
    var setupWizardSkippedForSession by rememberSaveable { mutableStateOf(false) }
    var setupWizardReturnPending by rememberSaveable { mutableStateOf(false) }
    var setupWizardScrollPosition by rememberSaveable { mutableStateOf(0) }
    val context = LocalContext.current
    val uiState by mainViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.appSettings.showSetupWizardOnStart) {
        if (
            uiState.appSettings.showSetupWizardOnStart &&
            !setupWizardSkippedForSession
        ) {
            setupWizardVisible = true
        }
    }

    LaunchedEffect(mainViewModel) {
        mainViewModel.events.collect { event ->
            when (event) {
                is MainUiEvent.ConfirmOpenAndroidWifiSettings -> {
                    pendingAndroidWifiSsid = event.ssid
                }

                MainUiEvent.OpenSetupWizard -> {
                    setupWizardSkippedForSession = false
                    setupWizardReturnPending = false
                    setupWizardVisible = true
                }
            }
        }
    }

    SideEffect {
        onNavigationStateChanged(currentScreen, helpReturnScreen, selectedSettingsSection)
    }

    BackHandler(enabled = currentScreen == AppScreen.Dashboard && setupWizardReturnPending) {
        setupWizardReturnPending = false
        setupWizardVisible = true
    }

    when (currentScreen) {
        AppScreen.Dashboard -> StartScreen(
            viewModel = mainViewModel,
            onNavigateToSettings = {
                currentScreen = AppScreen.Settings
            },
            onNavigateToUpdates = {
                selectedSettingsSection = SettingsSection.SYSTEM
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
                if (setupWizardReturnPending) {
                    setupWizardReturnPending = false
                    setupWizardVisible = true
                } else {
                    currentScreen = AppScreen.Dashboard
                }
            },
            onShowSetupWizard = mainViewModel::showSetupWizardAgain
        )

        AppScreen.Help -> HelpScreen(
            onNavigateBack = {
                if (setupWizardReturnPending) {
                    setupWizardReturnPending = false
                    setupWizardVisible = true
                } else {
                    currentScreen = helpReturnScreen
                }
            },
            onShowSetupWizard = mainViewModel::showSetupWizardAgain
        )

        AppScreen.About -> AboutScreen(
            onNavigateBack = { currentScreen = AppScreen.Dashboard }
        )
    }

    pendingAndroidWifiSsid?.let { ssid ->
        AlertDialog(
            onDismissRequest = { pendingAndroidWifiSsid = null },
            title = { Text(stringResource(R.string.android_managed_wifi_switch_title)) },
            text = { Text(stringResource(R.string.android_managed_wifi_switch_message, ssid)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingAndroidWifiSsid = null
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }) {
                    Text(stringResource(R.string.open_android_wifi_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAndroidWifiSsid = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (setupWizardVisible) {
        SetupWizard(
            initialScrollPosition = setupWizardScrollPosition,
            onOpenHelp = { scrollPosition ->
                setupWizardScrollPosition = scrollPosition
                setupWizardVisible = false
                setupWizardReturnPending = true
                helpReturnScreen = currentScreen
                currentScreen = AppScreen.Help
            },
            onOpenBackup = { scrollPosition ->
                setupWizardScrollPosition = scrollPosition
                setupWizardVisible = false
                setupWizardReturnPending = true
                selectedSettingsSection = SettingsSection.BACKUP
                currentScreen = AppScreen.Settings
            },
            onOpenWifiProfiles = { scrollPosition ->
                setupWizardScrollPosition = scrollPosition
                setupWizardVisible = false
                setupWizardReturnPending = true
                selectedSettingsSection = SettingsSection.WIFI_PROFILES
                currentScreen = AppScreen.Settings
            },
            onOpenDevices = { scrollPosition ->
                setupWizardScrollPosition = scrollPosition
                setupWizardVisible = false
                setupWizardReturnPending = true
                selectedSettingsSection = SettingsSection.DEVICES
                currentScreen = AppScreen.Settings
            },
            onOpenDashboard = { scrollPosition ->
                setupWizardScrollPosition = scrollPosition
                setupWizardVisible = false
                setupWizardReturnPending = true
                currentScreen = AppScreen.Dashboard
            },
            onSkip = {
                setupWizardVisible = false
                setupWizardSkippedForSession = true
                setupWizardReturnPending = false
            },
            onDoNotShowAgain = {
                setupWizardVisible = false
                setupWizardSkippedForSession = true
                setupWizardReturnPending = false
                mainViewModel.hideSetupWizardOnStart()
            }
        )
    }
}
