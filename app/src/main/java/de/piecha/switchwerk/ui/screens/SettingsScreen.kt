package de.piecha.switchwerk.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import de.piecha.switchwerk.data.repository.ConfigurationImportMode
import de.piecha.switchwerk.data.repository.ConfigurationImportSummary
import de.piecha.switchwerk.domain.model.WifiProfile
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
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scanQrCodeText = stringResource(R.string.scan_qr_code)
    val uiState by viewModel.uiState.collectAsState()
    var pendingFileImportMode by remember { mutableStateOf(ConfigurationImportMode.MERGE) }
    var pendingQrImportMode by remember { mutableStateOf(ConfigurationImportMode.MERGE) }
    var showFileImportModeDialog by remember { mutableStateOf(false) }
    var showQrImportModeDialog by remember { mutableStateOf(false) }
    var showUrlImportDialog by remember { mutableStateOf(false) }
    var showPasswordExportWarning by remember { mutableStateOf(false) }
    var openSwipeItemId by remember { mutableStateOf<String?>(null) }
    var pendingImportConfirmation by remember { mutableStateOf<(() -> Unit)?>(null) }

    val exportWithoutPasswordsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportConfiguration(it, includePasswords = false) }
    }
    val exportWithPasswordsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportConfiguration(it, includePasswords = true) }
    }
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.prepareImportFromFile(it, pendingFileImportMode) }
    }
    val qrScanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val content = result.contents
        if (content == null) {
            viewModel.reportQrScanCancelled()
        } else {
            viewModel.prepareImportFromQrCode(content, pendingQrImportMode)
        }
    }
    fun launchQrScanner() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt(scanQrCodeText)
            .setBeepEnabled(false)
            .setOrientationLocked(false)
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
                    openSwipeItemId = openSwipeItemId,
                    onOpenSwipeItem = { openSwipeItemId = it },
                    onCloseSwipeItem = { openSwipeItemId = null },
                    onAddClick = { runAfterClosingSwipe(viewModel::startNewWifiProfile) },
                    onEditClick = viewModel::startEditWifiProfile,
                    onDeleteClick = viewModel::deleteWifiProfile,
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
                        onExportClick = {
                            runAfterClosingSwipe {
                                viewModel.clearStatusMessage()
                                exportWithoutPasswordsLauncher.launch(EXPORT_FILE_NAME)
                            }
                        },
                        onExportWithPasswordsClick = {
                            runAfterClosingSwipe { showPasswordExportWarning = true }
                        },
                        onImportFileClick = {
                            runAfterClosingSwipe { showFileImportModeDialog = true }
                        },
                        onImportUrlClick = {
                            runAfterClosingSwipe { showUrlImportDialog = true }
                        },
                        onScanQrCodeClick = {
                            runAfterClosingSwipe { showQrImportModeDialog = true }
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
                exportWithPasswordsLauncher.launch(EXPORT_FILE_NAME)
            },
            onCancel = {
                showPasswordExportWarning = false
            }
        )
    }

    if (uiState.isEditingWifiProfile) {
        WifiProfileDialog(
            isNewProfile = uiState.form.id == null,
            name = uiState.form.name,
            ssid = uiState.form.ssid,
            password = uiState.form.password,
            isPasswordVisible = uiState.form.isPasswordVisible,
            errorMessage = uiState.errorMessage,
            onNameChange = viewModel::updateWifiProfileName,
            onSsidChange = viewModel::updateWifiProfileSsid,
            onPasswordChange = viewModel::updateWifiProfilePassword,
            onClearPasswordClick = viewModel::clearWifiProfilePassword,
            onTogglePasswordVisibility = viewModel::toggleWifiPasswordVisibility,
            onSaveClick = viewModel::saveWifiProfile,
            onCancelClick = viewModel::cancelWifiProfileEdit
        )
    }

    if (showFileImportModeDialog) {
        ImportModeDialog(
            continueText = stringResource(R.string.select_file),
            onContinue = { mode ->
                pendingFileImportMode = mode
                showFileImportModeDialog = false
                importFileLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
            },
            onCancel = {
                showFileImportModeDialog = false
            }
        )
    }

    if (showQrImportModeDialog) {
        ImportModeDialog(
            continueText = stringResource(R.string.scan_qr_code),
            onContinue = { mode ->
                pendingQrImportMode = mode
                showQrImportModeDialog = false
                if (
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    launchQrScanner()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onCancel = {
                showQrImportModeDialog = false
            }
        )
    }

    if (showUrlImportDialog) {
        UrlImportDialog(
            onImport = { url, mode ->
                showUrlImportDialog = false
                viewModel.prepareImportFromUrl(url, mode)
            },
            onCancel = {
                showUrlImportDialog = false
            }
        )
    }

    uiState.importSummary?.let { summary ->
        ImportSummaryDialog(
            summary = summary,
            mode = uiState.importMode ?: ConfigurationImportMode.MERGE,
            onImport = {
                confirmImportWithOptionalScanPermission(
                    importsWifiProfiles =
                        summary.wifiProfilesNew + summary.wifiProfilesOverwritten > 0,
                    confirmImport = viewModel::confirmImportSummary
                )
            },
            onCancel = viewModel::cancelPendingImport
        )
    }

    if (uiState.showImportPasswordWarning) {
        PasswordImportWarningDialog(
            onImport = viewModel::confirmPasswordImport,
            onCancel = viewModel::cancelPendingImport
        )
    }
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
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (WifiProfile) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
    var pendingDeleteProfile by remember { mutableStateOf<WifiProfile?>(null) }

    pendingDeleteProfile?.let { profileToDelete ->
        StandardConfigurationDialog(
            title = stringResource(R.string.delete_wifi_profile),
            onDismissRequest = { pendingDeleteProfile = null },
            actionText = stringResource(R.string.yes),
            onAction = {
                pendingDeleteProfile = null
                onDeleteClick()
            },
            cancelText = stringResource(R.string.no)
        ) {
            Text(stringResource(R.string.delete_wifi_profile_confirmation, profileToDelete.name))
        }
    }

    SwipeToDeleteListItem(
        isOpen = isOpen,
        isAnyItemOpen = isAnyItemOpen,
        onOpen = onOpen,
        onClose = onClose,
        onContentClick = onContentClick,
        onDeleteClick = {
            pendingDeleteProfile = profile
        }
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
    errorMessage: UiText?,
    onNameChange: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
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
            errorMessage = errorMessage,
            onNameChange = onNameChange,
            onSsidChange = onSsidChange,
            onPasswordChange = onPasswordChange,
            onClearPasswordClick = onClearPasswordClick,
            onTogglePasswordVisibility = onTogglePasswordVisibility
        )
    }
}

@Composable
private fun WifiProfileForm(
    name: String,
    ssid: String,
    password: String,
    isPasswordVisible: Boolean,
    errorMessage: UiText?,
    onNameChange: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
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
    onExportClick: () -> Unit,
    onExportWithPasswordsClick: () -> Unit,
    onImportFileClick: () -> Unit,
    onImportUrlClick: () -> Unit,
    onScanQrCodeClick: () -> Unit
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
                Text(stringResource(R.string.export), style = MaterialTheme.typography.titleSmall)
                StandardActionButton(
                    text = stringResource(R.string.export_configuration),
                    onClick = onExportClick,
                    modifier = Modifier.fillMaxWidth()
                )
                StandardActionButton(
                    text = stringResource(R.string.export_with_passwords),
                    onClick = onExportWithPasswordsClick,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(stringResource(R.string.import_title), style = MaterialTheme.typography.titleSmall)
                StandardActionButton(
                    text = stringResource(R.string.import_file),
                    onClick = onImportFileClick,
                    modifier = Modifier.fillMaxWidth()
                )
                StandardActionButton(
                    text = stringResource(R.string.import_url),
                    onClick = onImportUrlClick,
                    modifier = Modifier.fillMaxWidth()
                )
                StandardActionButton(
                    text = stringResource(R.string.import_qr_code),
                    onClick = onScanQrCodeClick,
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
private fun ImportModeDialog(
    continueText: String,
    onContinue: (ConfigurationImportMode) -> Unit,
    onCancel: () -> Unit
) {
    var mode by remember { mutableStateOf(ConfigurationImportMode.MERGE) }
    StandardConfigurationDialog(
        title = stringResource(R.string.choose_import_mode),
        onDismissRequest = onCancel,
        actionText = continueText,
        onAction = { onContinue(mode) }
    ) {
        ImportModeSelection(
            mode = mode,
            onModeChange = { mode = it }
        )
    }
}

@Composable
private fun UrlImportDialog(
    onImport: (String, ConfigurationImportMode) -> Unit,
    onCancel: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var url by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(ConfigurationImportMode.MERGE) }
    StandardConfigurationDialog(
        title = stringResource(R.string.import_from_https),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.check_import),
        onAction = { onImport(url, mode) },
        actionEnabled = url.isNotBlank()
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text(stringResource(R.string.https_url)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        ImportModeSelection(
            mode = mode,
            onModeChange = { mode = it }
        )
    }
}

@Composable
private fun ImportModeSelection(
    mode: ConfigurationImportMode,
    onModeChange: (ConfigurationImportMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ImportModeOption(
            selected = mode == ConfigurationImportMode.MERGE,
            title = stringResource(R.string.import_mode_merge),
            description = stringResource(R.string.import_mode_merge_description),
            onClick = { onModeChange(ConfigurationImportMode.MERGE) }
        )
        ImportModeOption(
            selected = mode == ConfigurationImportMode.REPLACE,
            title = stringResource(R.string.import_mode_replace),
            description = stringResource(R.string.import_mode_replace_description),
            onClick = { onModeChange(ConfigurationImportMode.REPLACE) }
        )
    }
}

@Composable
private fun ImportModeOption(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
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
private fun ImportSummaryDialog(
    summary: ConfigurationImportSummary,
    mode: ConfigurationImportMode,
    onImport: () -> Unit,
    onCancel: () -> Unit
) {
    val wifiProfilesText = stringResource(
        R.string.import_summary_wifi_profiles,
        summary.wifiProfilesNew,
        summary.wifiProfilesOverwritten
    )
    val devicesText = stringResource(
        R.string.import_summary_devices,
        summary.devicesNew,
        summary.devicesOverwritten
    )
    val passwordsText = stringResource(
        R.string.import_summary_passwords,
        summary.passwordsIncluded,
        summary.passwordsDeleted
    )
    val deletedWifiProfilesText = stringResource(
        R.string.import_summary_local_wifi_profiles_deleted,
        summary.localWifiProfilesDeleted
    )
    val deletedDevicesText = stringResource(
        R.string.import_summary_local_devices_deleted,
        summary.localDevicesDeleted
    )
    val text = buildString {
        appendLine(wifiProfilesText)
        appendLine(devicesText)
        appendLine(passwordsText)
        if (mode == ConfigurationImportMode.REPLACE) {
            appendLine()
            appendLine(deletedWifiProfilesText)
            append(deletedDevicesText)
        }
    }
    StandardConfigurationDialog(
        title = stringResource(R.string.import_summary_title),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.import_action),
        onAction = onImport
    ) {
        Text(text)
    }
}

@Composable
private fun PasswordImportWarningDialog(
    onImport: () -> Unit,
    onCancel: () -> Unit
) {
    StandardConfigurationDialog(
        title = stringResource(R.string.import_passwords_title),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.import_action),
        onAction = onImport
    ) {
        Text(stringResource(R.string.import_passwords_warning))
    }
}

private const val EXPORT_FILE_NAME = "switchwerk-config.json"
