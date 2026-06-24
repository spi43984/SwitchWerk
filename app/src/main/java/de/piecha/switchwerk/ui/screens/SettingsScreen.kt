package de.piecha.switchwerk.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.provider.OpenableColumns
import android.view.inputmethod.InputMethodManager
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import de.piecha.switchwerk.data.repository.ConfigurationImportMode
import de.piecha.switchwerk.data.repository.ConfigurationImportSummary
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.domain.model.WifiConnectionMode
import de.piecha.switchwerk.domain.model.WifiProfileSortCriterion
import de.piecha.switchwerk.domain.model.WifiProfileSortDirection
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.AppLanguage
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.ui.components.SettingsSectionTabs
import de.piecha.switchwerk.ui.components.InfoHint
import de.piecha.switchwerk.ui.components.StandardActionButton
import de.piecha.switchwerk.ui.components.StandardConfigurationDialog
import de.piecha.switchwerk.ui.components.SwipeToDeleteListItem
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.ui.asString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    selectedSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit,
    onNavigateBack: () -> Unit,
    initialUiState: SettingsScreenUiState,
    onUiStateChanged: (SettingsScreenUiState) -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scanQrCodeText = stringResource(R.string.scan_qr_code)
    val uiState by viewModel.uiState.collectAsState()
    var importMode by remember { mutableStateOf(initialUiState.importMode) }
    var urlImportValue by remember { mutableStateOf(initialUiState.urlImportValue) }
    var fileImportUri by remember { mutableStateOf(initialUiState.fileImportUri) }
    var fileImportReference by remember { mutableStateOf(initialUiState.fileImportReference) }
    var showImportConfigurationDialog by remember {
        mutableStateOf(initialUiState.showImportConfigurationDialog)
    }
    var importSource by remember { mutableStateOf(initialUiState.importSource) }
    var importPasswords by remember { mutableStateOf(false) }
    var showPasswordExportWarning by remember { mutableStateOf(initialUiState.showPasswordExportWarning) }
    var exportIncludesPasswords by remember { mutableStateOf(initialUiState.exportIncludesPasswords) }
    var openSwipeItemId by remember { mutableStateOf(initialUiState.openSwipeItemId) }
    var pendingImportConfirmation by remember { mutableStateOf<(() -> Unit)?>(null) }

    SideEffect {
        onUiStateChanged(
            SettingsScreenUiState(
                importMode = importMode,
                urlImportValue = urlImportValue,
                fileImportUri = fileImportUri,
                fileImportReference = fileImportReference,
                showImportConfigurationDialog = showImportConfigurationDialog,
                importSource = importSource,
                showPasswordExportWarning = showPasswordExportWarning,
                exportIncludesPasswords = exportIncludesPasswords,
                openSwipeItemId = openSwipeItemId
            )
        )
    }

    val exportConfigurationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportConfiguration(it, includePasswords = exportIncludesPasswords) }
    }
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            importSource = ImportSource.FILE
            fileImportUri = it.toString()
            fileImportReference = it.displayReference(context)
        }
    }
    val qrScanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val content = result.contents
        if (content == null) {
            viewModel.reportQrScanCancelled()
        } else {
            viewModel.prepareImportFromQrCode(content, importMode)
        }
    }
    fun launchQrScanner() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt(scanQrCodeText)
            .setBeepEnabled(false)
            .setOrientationLocked(true)
        qrScanLauncher.launch(options)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchQrScanner()
        } else {
            viewModel.reportQrCameraPermissionDenied()
        }
    }
    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        pendingImportConfirmation?.invoke()
        pendingImportConfirmation = null
    }
    fun confirmImportWithOptionalScanPermission(
        importsWifiProfiles: Boolean,
        confirmImport: () -> Unit
    ) {
        if (
            !importsWifiProfiles ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            confirmImport()
            return
        }
        pendingImportConfirmation = confirmImport
        locationPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
    fun runAfterClosingSwipe(action: () -> Unit) {
        if (openSwipeItemId != null) {
            openSwipeItemId = null
        } else {
            action()
        }
    }

    BackHandler(enabled = uiState.isEditingWifiProfile) {
        viewModel.cancelWifiProfileEdit()
    }

    BackHandler(enabled = uiState.isEditingDevice) {
        viewModel.cancelDeviceEdit()
    }

    BackHandler(enabled = !uiState.isEditingWifiProfile && !uiState.isEditingDevice) {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .clickable(
                enabled = openSwipeItemId != null,
                onClick = {
                    openSwipeItemId = null
                }
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_to_dashboard)
                )
            }
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineLarge
            )
            InfoHint(R.string.settings, R.string.settings_info)
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        uiState.statusMessage?.let { message ->
            Text(
                text = message.asString(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SettingsSectionTabs(
            sections = SettingsSection.entries.map { stringResource(it.titleResourceId) },
            selectedIndex = selectedSection.ordinal,
            onSectionSelected = { index ->
                openSwipeItemId = null
                onSectionSelected(SettingsSection.entries[index])
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedSection) {
                SettingsSection.WIFI_PROFILES -> WifiProfileManagementSection(
                    profiles = uiState.wifiProfiles,
                    sortCriterion = uiState.appSettings.wifiProfileSortCriterion,
                    sortDirection = uiState.appSettings.wifiProfileSortDirection,
                    openSwipeItemId = openSwipeItemId,
                    onOpenSwipeItem = { openSwipeItemId = it },
                    onCloseSwipeItem = { openSwipeItemId = null },
                    onAddClick = { runAfterClosingSwipe(viewModel::startNewWifiProfile) },
                    onSortingChange = viewModel::setWifiProfileSorting,
                    onEditClick = viewModel::startEditWifiProfile,
                    onDeleteClick = viewModel::requestWifiProfileDeletion,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsSection.DEVICES -> DeviceManagementSection(
                    devices = uiState.devices,
                    wifiProfiles = uiState.wifiProfiles,
                    isEditing = uiState.isEditingDevice,
                    form = uiState.deviceForm,
                    openSwipeItemId = openSwipeItemId,
                    onOpenSwipeItem = { openSwipeItemId = it },
                    onCloseSwipeItem = { openSwipeItemId = null },
                    onAddClick = { runAfterClosingSwipe(viewModel::startNewDevice) },
                    onEditClick = viewModel::startEditDevice,
                    onDeleteClick = viewModel::deleteDevice,
                    onNameChange = viewModel::updateDeviceName,
                    onActionLabelChange = viewModel::updateDeviceActionLabel,
                    onApiMethodChange = viewModel::updateDeviceApiMethod,
                    onApiPathChange = viewModel::updateDeviceApiPath,
                    onAddConnection = viewModel::addDeviceConnection,
                    onUpdateConnection = viewModel::updateDeviceConnection,
                    onDeleteConnection = viewModel::deleteDeviceConnection,
                    onMoveConnection = viewModel::moveDeviceConnection,
                    onSaveClick = viewModel::saveDevice,
                    onCancelClick = viewModel::cancelDeviceEdit,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsSection.SYSTEM -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DisplaySettingsSection(
                        themeMode = uiState.appSettings.themeMode,
                        language = uiState.appSettings.language,
                        onLanguageChange = viewModel::setLanguage,
                        onThemeModeChange = viewModel::setThemeMode,
                        showInfoHint = true
                    )
                    HorizontalDivider()
                    ActionDetailsSettingsSection(
                        showActionDetails = uiState.appSettings.showActionDetails,
                        detailPanelHeight = uiState.appSettings.detailPanelHeight,
                        diagnosticsNewestFirst = uiState.appSettings.diagnosticsNewestFirst,
                        onShowActionDetailsChange = viewModel::setShowActionDetails,
                        onDetailPanelHeightChange = viewModel::setDetailPanelHeight,
                        onDiagnosticsNewestFirstChange = viewModel::setDiagnosticsNewestFirst
                    )
                }

                SettingsSection.BACKUP -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ImportExportSection(
                        isTransferInProgress = uiState.isTransferInProgress,
                        includePasswords = exportIncludesPasswords,
                        onIncludePasswordsChange = { exportIncludesPasswords = it },
                        onExportClick = {
                            runAfterClosingSwipe {
                                viewModel.clearStatusMessage()
                                if (exportIncludesPasswords) {
                                    showPasswordExportWarning = true
                                } else {
                                    exportConfigurationLauncher.launch(EXPORT_FILE_NAME)
                                }
                            }
                        },
                        onImportClick = {
                            runAfterClosingSwipe {
                                importPasswords = false
                                showImportConfigurationDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showPasswordExportWarning) {
        PasswordExportWarningDialog(
            onExport = {
                showPasswordExportWarning = false
                exportConfigurationLauncher.launch(EXPORT_FILE_NAME)
            },
            onCancel = {
                showPasswordExportWarning = false
            }
        )
    }

    uiState.wifiProfileDeletionConfirmation?.let { confirmation ->
        StandardConfigurationDialog(
            title = stringResource(R.string.delete_wifi_profile),
            onDismissRequest = viewModel::cancelWifiProfileDeletion,
            actionText = stringResource(R.string.yes),
            onAction = viewModel::confirmWifiProfileDeletion,
            cancelText = stringResource(R.string.no),
        ) {
            Text(stringResource(R.string.delete_wifi_profile_confirmation, confirmation.profile.name))
            if (confirmation.affectedDeviceNames.isNotEmpty()) {
                Text(
                    pluralStringResource(
                        R.plurals.delete_wifi_profile_used_warning,
                        confirmation.affectedDeviceNames.size,
                        confirmation.affectedDeviceNames.size
                    )
                )
                Text(stringResource(R.string.affected_devices))
                confirmation.affectedDeviceNames.forEach { deviceName ->
                    Text(deviceName)
                }
            }
        }
    }

    if (uiState.isEditingWifiProfile) {
        WifiProfileDialog(
            isNewProfile = uiState.form.id == null,
            name = uiState.form.name,
            ssid = uiState.form.ssid,
            password = uiState.form.password,
            isPasswordVisible = uiState.form.isPasswordVisible,
            connectionMode = uiState.form.connectionMode,
            visibleSsids = uiState.form.visibleSsids,
            errorMessage = uiState.errorMessage,
            onNameChange = viewModel::updateWifiProfileName,
            onSsidChange = viewModel::updateWifiProfileSsid,
            onPasswordChange = viewModel::updateWifiProfilePassword,
            onClearPasswordClick = viewModel::clearWifiProfilePassword,
            onTogglePasswordVisibility = viewModel::toggleWifiPasswordVisibility,
            onConnectionModeChange = viewModel::updateWifiConnectionMode,
            onLoadVisibleSsids = viewModel::loadVisibleSsids,
            onOpenAndroidWifiSettings = {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            },
            onSaveClick = viewModel::saveWifiProfile,
            onCancelClick = viewModel::cancelWifiProfileEdit
        )
    }

    if (showImportConfigurationDialog) {
        ImportConfigurationDialog(
            source = importSource,
            url = urlImportValue,
            fileUri = fileImportUri,
            fileReference = fileImportReference,
            isPreparing = uiState.isTransferInProgress,
            summary = uiState.importSummary,
            mode = uiState.importMode ?: importMode,
            importPasswords = importPasswords,
            onSelectUrl = {
                importSource = ImportSource.URL
                importPasswords = false
                viewModel.cancelPendingImport()
            },
            onPickFile = {
                importPasswords = false
                viewModel.cancelPendingImport()
                importFileLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
            },
            onScanQrCode = {
                importSource = ImportSource.QR_CODE
                importPasswords = false
                viewModel.cancelPendingImport()
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    launchQrScanner()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onUrlChange = { urlImportValue = it },
            onLoadUrl = { viewModel.prepareImportFromUrl(urlImportValue, importMode) },
            onLoadFile = { viewModel.prepareImportFromFile(Uri.parse(fileImportUri), importMode) },
            onModeChange = { mode ->
                importMode = mode
                viewModel.updateImportMode(mode)
            },
            onImportPasswordsChange = { importPasswords = it },
            onImport = { summary ->
                showImportConfigurationDialog = false
                confirmImportWithOptionalScanPermission(
                    importsWifiProfiles =
                        summary.wifiProfilesNew + summary.wifiProfilesOverwritten > 0,
                    confirmImport = { viewModel.confirmImport(importPasswords) }
                )
            },
            onCancel = {
                showImportConfigurationDialog = false
                viewModel.cancelPendingImport()
            }
        )
    }
}

data class SettingsScreenUiState(
    val importMode: ConfigurationImportMode = ConfigurationImportMode.MERGE,
    val urlImportValue: String = "",
    val fileImportUri: String = "",
    val fileImportReference: String = "",
    val showImportConfigurationDialog: Boolean = false,
    val importSource: ImportSource? = null,
    val showPasswordExportWarning: Boolean = false,
    val exportIncludesPasswords: Boolean = false,
    val openSwipeItemId: String? = null
)

enum class ImportSource {
    FILE,
    URL,
    QR_CODE
}

enum class SettingsSection(val titleResourceId: Int) {
    WIFI_PROFILES(R.string.settings_tab_wifi_profiles),
    DEVICES(R.string.settings_tab_devices),
    SYSTEM(R.string.settings_tab_system),
    BACKUP(R.string.settings_tab_backup)
}

@Composable
private fun DisplaySettingsSection(
    themeMode: AppThemeMode,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    showInfoHint: Boolean = false
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 36.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.display), style = MaterialTheme.typography.titleMedium)
                if (showInfoHint) {
                    InfoHint(R.string.system_info_title, R.string.system_info)
                }
            }
            AppThemeMode.entries.forEach { option ->
                RadioOptionRow(
                    label = when (option) {
                        AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                        AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
                        AppThemeMode.DARK -> stringResource(R.string.theme_dark)
                    },
                    selected = themeMode == option,
                    onClick = { onThemeModeChange(option) }
                )
            }
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleSmall)
            AppLanguage.entries.forEach { option ->
                RadioOptionRow(
                    label = when (option) {
                        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                        AppLanguage.GERMAN -> stringResource(R.string.language_german)
                        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                    },
                    selected = language == option,
                    onClick = { onLanguageChange(option) }
                )
            }
        }
    }
}

@Composable
private fun ActionDetailsSettingsSection(
    showActionDetails: Boolean,
    detailPanelHeight: DetailPanelHeight,
    diagnosticsNewestFirst: Boolean,
    onShowActionDetailsChange: (Boolean) -> Unit,
    onDetailPanelHeightChange: (DetailPanelHeight) -> Unit,
    onDiagnosticsNewestFirstChange: (Boolean) -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 36.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(stringResource(R.string.action_details), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clickable { onShowActionDetailsChange(!showActionDetails) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.show_action_details))
                Switch(
                    checked = showActionDetails,
                    onCheckedChange = onShowActionDetailsChange
                )
            }

            Text(stringResource(R.string.detail_panel_height), style = MaterialTheme.typography.titleSmall)
            DetailPanelHeight.entries.forEach { option ->
                RadioOptionRow(
                    label = when (option) {
                        DetailPanelHeight.TWENTY_PERCENT -> stringResource(R.string.detail_panel_height_20)
                        DetailPanelHeight.THIRTY_PERCENT -> stringResource(R.string.detail_panel_height_30)
                        DetailPanelHeight.FORTY_PERCENT -> stringResource(R.string.detail_panel_height_40)
                    },
                    selected = detailPanelHeight == option,
                    onClick = { onDetailPanelHeightChange(option) },
                    enabled = showActionDetails
                )
            }

            Text(
                text = stringResource(R.string.action_details_sort_order),
                color = if (showActionDetails) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                style = MaterialTheme.typography.titleSmall
            )
            RadioOptionRow(
                label = stringResource(R.string.newest_first),
                selected = diagnosticsNewestFirst,
                onClick = { onDiagnosticsNewestFirstChange(true) },
                enabled = showActionDetails
            )
            RadioOptionRow(
                label = stringResource(R.string.newest_last),
                selected = !diagnosticsNewestFirst,
                onClick = { onDiagnosticsNewestFirstChange(false) },
                enabled = showActionDetails
            )
        }
    }
}

@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            modifier = Modifier.size(32.dp),
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Text(
            text = label,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}

@Composable
private fun WifiProfileManagementSection(
    profiles: List<WifiProfile>,
    sortCriterion: WifiProfileSortCriterion,
    sortDirection: WifiProfileSortDirection,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onAddClick: () -> Unit,
    onSortingChange: (WifiProfileSortCriterion, WifiProfileSortDirection) -> Unit,
    onEditClick: (WifiProfile) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.name_and_ssid),
                style = MaterialTheme.typography.titleSmall
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoHint(R.string.wifi_profiles_info_title, R.string.list_interaction_info)
                Box {
                    IconButton(
                        onClick = { isSortMenuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(
                                R.string.wifi_profile_sort_current,
                                stringResource(sortCriterion.sortOptionLabelResourceId(sortDirection))
                            )
                        )
                    }
                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        WifiProfileSortCriterion.entries.forEach { criterion ->
                            WifiProfileSortDirection.entries.forEach { direction ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            criterion.sortOptionLabelResourceId(direction)
                                        )
                                    )
                                },
                                onClick = {
                                    onSortingChange(criterion, direction)
                                    isSortMenuExpanded = false
                                },
                                trailingIcon = {
                                    RadioButton(
                                        selected = criterion == sortCriterion && direction == sortDirection,
                                        onClick = null
                                    )
                                }
                            )
                            }
                        }
                    }
                }
                IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_wifi_profile)
                    )
                }
            }
        }

        WifiProfileList(
            profiles = profiles,
            openSwipeItemId = openSwipeItemId,
            onOpenSwipeItem = onOpenSwipeItem,
            onCloseSwipeItem = onCloseSwipeItem,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@androidx.annotation.StringRes
private fun WifiProfileSortCriterion.sortOptionLabelResourceId(
    direction: WifiProfileSortDirection
): Int = when (this) {
    WifiProfileSortCriterion.PROFILE_NAME -> when (direction) {
        WifiProfileSortDirection.ASCENDING -> R.string.wifi_profile_sort_profile_name_ascending
        WifiProfileSortDirection.DESCENDING -> R.string.wifi_profile_sort_profile_name_descending
    }
    WifiProfileSortCriterion.SSID -> when (direction) {
        WifiProfileSortDirection.ASCENDING -> R.string.wifi_profile_sort_ssid_ascending
        WifiProfileSortDirection.DESCENDING -> R.string.wifi_profile_sort_ssid_descending
    }
}

@Composable
private fun WifiProfileList(
    profiles: List<WifiProfile>,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onEditClick: (WifiProfile) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (profiles.isEmpty()) {
        EmptyWifiProfileListArea(modifier)
        return
    }

    val listState = rememberLazyListState()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (listState.canScrollBackward) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.more_wifi_profiles_above),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .weight(1f)
                .clickable(
                    enabled = openSwipeItemId != null,
                    onClick = onCloseSwipeItem
                )
        ) {
            items(
                items = profiles,
                key = { profile -> profile.id }
            ) { profile ->
                val swipeItemId = "wifi:${profile.id}"
                WifiProfileRow(
                    profile = profile,
                    isOpen = openSwipeItemId == swipeItemId,
                    isAnyItemOpen = openSwipeItemId != null,
                    onOpen = {
                        onOpenSwipeItem(swipeItemId)
                    },
                    onClose = onCloseSwipeItem,
                    onContentClick = {
                        if (openSwipeItemId == null) {
                            onEditClick(profile)
                        } else {
                            onCloseSwipeItem()
                        }
                    },
                    onDeleteClick = { onDeleteClick(profile.id) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (listState.canScrollForward) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.more_wifi_profiles_below),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyWifiProfileListArea(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = stringResource(R.string.no_wifi_profiles_configured),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun WifiProfileRow(
    profile: WifiProfile,
    isOpen: Boolean,
    isAnyItemOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onContentClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    SwipeToDeleteListItem(
        isOpen = isOpen,
        isAnyItemOpen = isAnyItemOpen,
        onOpen = onOpen,
        onClose = onClose,
        onContentClick = onContentClick,
        onDeleteClick = onDeleteClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = stringResource(R.string.ssid_value, profile.ssid),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun WifiProfileDialog(
    isNewProfile: Boolean,
    name: String,
    ssid: String,
    password: String,
    isPasswordVisible: Boolean,
    connectionMode: WifiConnectionMode,
    visibleSsids: List<String>,
    errorMessage: UiText?,
    onNameChange: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onConnectionModeChange: (WifiConnectionMode) -> Unit,
    onLoadVisibleSsids: () -> Unit,
    onOpenAndroidWifiSettings: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    StandardConfigurationDialog(
        title = stringResource(
            if (isNewProfile) R.string.create_wifi_profile else R.string.edit_wifi_profile
        ),
        onDismissRequest = onCancelClick,
        actionText = stringResource(R.string.save),
        onAction = onSaveClick,
        infoTitleResourceId = R.string.wifi_profile_dialog_info_title,
        infoMessageResourceId = R.string.wifi_profile_dialog_info
    ) {
        WifiProfileForm(
            name = name,
            ssid = ssid,
            password = password,
            isPasswordVisible = isPasswordVisible,
            connectionMode = connectionMode,
            visibleSsids = visibleSsids,
            errorMessage = errorMessage,
            onNameChange = onNameChange,
            onSsidChange = onSsidChange,
            onPasswordChange = onPasswordChange,
            onClearPasswordClick = onClearPasswordClick,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            onConnectionModeChange = onConnectionModeChange,
            onLoadVisibleSsids = onLoadVisibleSsids,
            onOpenAndroidWifiSettings = onOpenAndroidWifiSettings
        )
    }
}

@Composable
private fun WifiProfileForm(
    name: String,
    ssid: String,
    password: String,
    isPasswordVisible: Boolean,
    connectionMode: WifiConnectionMode,
    visibleSsids: List<String>,
    errorMessage: UiText?,
    onNameChange: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onConnectionModeChange: (WifiConnectionMode) -> Unit,
    onLoadVisibleSsids: () -> Unit,
    onOpenAndroidWifiSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val nameFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = ssid,
            onValueChange = onSsidChange,
            label = { Text(stringResource(R.string.ssid)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        WifiConnectionMode.entries.forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = connectionMode == mode,
                    onClick = { onConnectionModeChange(mode) }
                )
                Text(
                    text = stringResource(
                        if (mode == WifiConnectionMode.SWITCHWERK_MANAGED) {
                            R.string.wifi_connection_mode_switchwerk_managed
                        } else {
                            R.string.wifi_connection_mode_android_managed
                        }
                    )
                )
            }
        }

        StandardActionButton(
            text = stringResource(R.string.select_visible_wifi_ssid),
            onClick = onLoadVisibleSsids,
            modifier = Modifier.fillMaxWidth()
        )
        visibleSsids.forEach { visibleSsid ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = ssid == visibleSsid, onClick = { onSsidChange(visibleSsid) })
                Text(visibleSsid)
            }
        }

        if (connectionMode == WifiConnectionMode.SWITCHWERK_MANAGED) {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { nameFocusRequester.requestFocus() }),
            trailingIcon = {
                IconButton(
                    onClick = onTogglePasswordVisibility
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = stringResource(R.string.toggle_password_visibility)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester)
        )

        Text(
            text = stringResource(R.string.password_hint),
            style = MaterialTheme.typography.bodySmall
        )

        StandardActionButton(
            text = stringResource(R.string.clear_password),
            onClick = onClearPasswordClick,
            modifier = Modifier.fillMaxWidth()
        )
        } else {
            Text(
                text = stringResource(R.string.android_managed_wifi_info),
                style = MaterialTheme.typography.bodySmall
            )
            StandardActionButton(
                text = stringResource(R.string.open_android_wifi_settings),
                onClick = onOpenAndroidWifiSettings,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocusRequester)
        )

        errorMessage?.let { message ->
            Text(
                text = message.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ImportExportSection(
    isTransferInProgress: Boolean,
    includePasswords: Boolean,
    onIncludePasswordsChange: (Boolean) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = stringResource(R.string.backup_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            InfoHint(R.string.settings_tab_backup, R.string.backup_info)
        }

        if (isTransferInProgress) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(stringResource(R.string.configuration_processing))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clickable { onIncludePasswordsChange(!includePasswords) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.include_passwords))
                    Switch(
                        checked = includePasswords,
                        onCheckedChange = onIncludePasswordsChange
                    )
                }
                StandardActionButton(
                    text = stringResource(R.string.export_configuration),
                    onClick = onExportClick,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StandardActionButton(
                    text = stringResource(R.string.import_configuration),
                    onClick = onImportClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PasswordExportWarningDialog(
    onExport: () -> Unit,
    onCancel: () -> Unit
) {
    StandardConfigurationDialog(
        title = stringResource(R.string.export_passwords_title),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.export_passwords_action),
        onAction = onExport
    ) {
        Text(stringResource(R.string.export_passwords_warning))
    }
}

@Composable
private fun ImportModeOption(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ImportSummaryContent(
    summary: ConfigurationImportSummary,
    mode: ConfigurationImportMode,
    importPasswords: Boolean
) {
    val wifiProfilesText = if (mode == ConfigurationImportMode.REPLACE) {
        stringResource(
            R.string.import_summary_replace_value,
            summary.wifiProfilesNew,
            summary.localWifiProfilesDeleted
        )
    } else {
        stringResource(
            R.string.import_summary_merge_value,
            summary.wifiProfilesNew,
            summary.wifiProfilesOverwritten
        )
    }
    val devicesText = if (mode == ConfigurationImportMode.REPLACE) {
        stringResource(
            R.string.import_summary_replace_value,
            summary.devicesNew,
            summary.localDevicesDeleted
        )
    } else {
        stringResource(
            R.string.import_summary_merge_value,
            summary.devicesNew,
            summary.devicesOverwritten
        )
    }
    val passwordsText = when {
        summary.passwordsIncluded + summary.passwordsDeleted == 0 -> {
            stringResource(R.string.import_summary_no_password_fields)
        }
        importPasswords -> stringResource(
            R.string.import_summary_passwords_enabled,
            summary.passwordsIncluded,
            summary.passwordsDeleted
        )
        else -> stringResource(
            R.string.import_summary_passwords_disabled,
            summary.passwordsIncluded + summary.passwordsDeleted
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ImportSummaryRow(stringResource(R.string.import_summary_wifi_profiles_label), wifiProfilesText)
        ImportSummaryRow(stringResource(R.string.import_summary_devices_label), devicesText)
        ImportSummaryRow(stringResource(R.string.import_summary_passwords_label), passwordsText)
    }
}

@Composable
private fun ImportSummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.65f)
        )
    }
}

@Composable
private fun ImportConfigurationDialog(
    source: ImportSource?,
    url: String,
    fileUri: String,
    fileReference: String,
    isPreparing: Boolean,
    summary: ConfigurationImportSummary?,
    mode: ConfigurationImportMode,
    importPasswords: Boolean,
    onSelectUrl: () -> Unit,
    onPickFile: () -> Unit,
    onScanQrCode: () -> Unit,
    onUrlChange: (String) -> Unit,
    onLoadUrl: () -> Unit,
    onLoadFile: () -> Unit,
    onModeChange: (ConfigurationImportMode) -> Unit,
    onImportPasswordsChange: (Boolean) -> Unit,
    onImport: (ConfigurationImportSummary) -> Unit,
    onCancel: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val view = LocalView.current
    val importModeFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isPreparing) {
        if (isPreparing) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            context.getSystemService(InputMethodManager::class.java)
                ?.hideSoftInputFromWindow(view.windowToken, 0)
            ViewCompat.getWindowInsetsController(view)?.hide(WindowInsetsCompat.Type.ime())
        }
    }
    val containsPasswordFields = summary?.let {
        it.passwordsIncluded + it.passwordsDeleted > 0
    } == true
    StandardConfigurationDialog(
        title = stringResource(R.string.import_configuration),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.import_action),
        onAction = { summary?.let(onImport) },
        actionEnabled = summary != null && !isPreparing,
        scrollToBottom = summary != null
    ) {
        Text(stringResource(R.string.import_step_source), style = MaterialTheme.typography.titleSmall)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 32.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SourceOptionRow(
                    title = stringResource(R.string.import_file),
                    selected = source == ImportSource.FILE,
                    onClick = onPickFile
                )
                SourceOptionRow(
                    title = stringResource(R.string.import_url),
                    selected = source == ImportSource.URL,
                    onClick = onSelectUrl
                )
                SourceOptionRow(
                    title = stringResource(R.string.import_qr_code),
                    selected = source == ImportSource.QR_CODE,
                    onClick = onScanQrCode
                )
            }
        }
        if (source == ImportSource.URL) {
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text(stringResource(R.string.https_url)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            StandardActionButton(
                text = stringResource(R.string.load_configuration),
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    context.getSystemService(InputMethodManager::class.java)
                        ?.hideSoftInputFromWindow(view.windowToken, 0)
                    importModeFocusRequester.requestFocus()
                    onLoadUrl()
                },
                enabled = url.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        } else if (source == ImportSource.FILE) {
            OutlinedTextField(
                value = fileReference,
                onValueChange = {},
                label = { Text(stringResource(R.string.selected_file)) },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            StandardActionButton(
                text = stringResource(R.string.load_configuration),
                onClick = {
                    importModeFocusRequester.requestFocus()
                    onLoadFile()
                },
                enabled = fileUri.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider()
        Text(stringResource(R.string.import_step_options), style = MaterialTheme.typography.titleSmall)
        ImportModeOption(
            selected = mode == ConfigurationImportMode.MERGE,
            title = stringResource(R.string.import_mode_merge),
            description = stringResource(R.string.import_mode_merge_description),
            onClick = { onModeChange(ConfigurationImportMode.MERGE) },
            modifier = Modifier
                .focusRequester(importModeFocusRequester)
                .focusable()
        )
        ImportModeOption(
            selected = mode == ConfigurationImportMode.REPLACE,
            title = stringResource(R.string.import_mode_replace),
            description = stringResource(R.string.import_mode_replace_description),
            onClick = { onModeChange(ConfigurationImportMode.REPLACE) }
        )
        if (containsPasswordFields) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onImportPasswordsChange(!importPasswords) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.import_passwords_switch))
                Switch(
                    checked = importPasswords,
                    onCheckedChange = onImportPasswordsChange
                )
            }
            Text(
                text = stringResource(
                    if (importPasswords) {
                        R.string.import_passwords_enabled_hint
                    } else {
                        R.string.import_passwords_disabled_hint
                    }
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }

        HorizontalDivider()
        Text(stringResource(R.string.import_step_summary), style = MaterialTheme.typography.titleSmall)
        if (isPreparing) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(stringResource(R.string.configuration_processing))
            }
        } else if (summary == null) {
            Text(stringResource(R.string.import_select_source_hint))
        } else {
            ImportSummaryContent(summary, mode, importPasswords)
        }
    }
}

@Composable
private fun SourceOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(title, modifier = Modifier.padding(start = 6.dp))
    }
}

private fun Uri.displayReference(context: Context): String {
    val displayName = context.contentResolver.query(
        this,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (column >= 0 && cursor.moveToFirst()) cursor.getString(column) else null
    }
    val decodedPath = path?.let(Uri::decode)?.trimEnd('/')
    return when {
        decodedPath != null && displayName != null -> {
            if (decodedPath.endsWith("/$displayName")) decodedPath else "$decodedPath/$displayName"
        }
        decodedPath != null -> decodedPath
        displayName != null -> displayName
        else -> toString()
    }
}

private const val EXPORT_FILE_NAME = "switchwerk-config.json"
