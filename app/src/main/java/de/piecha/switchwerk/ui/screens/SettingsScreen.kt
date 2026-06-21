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
import androidx.compose.ui.platform.LocalContext
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
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.ui.components.SettingsSectionTabs
import de.piecha.switchwerk.ui.components.StandardActionButton
import de.piecha.switchwerk.ui.components.StandardConfigurationDialog
import de.piecha.switchwerk.ui.components.SwipeToDeleteListItem
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    selectedSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
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
            .setPrompt("QR-Code scannen")
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Zurück zum Dashboard"
                )
            }
            Text(
                text = "Einstellungen",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        uiState.statusMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SettingsSectionTabs(
            sections = SettingsSection.entries.map(SettingsSection::title),
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
                        onThemeModeChange = viewModel::setThemeMode
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
                    HorizontalDivider()
                    SystemHelpSection(onOpenHelp = onNavigateToHelp)
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
            continueText = "Datei auswählen",
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
            continueText = "QR-Code scannen",
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

enum class SettingsSection(val title: String) {
    WIFI_PROFILES("WLAN-Profile"),
    DEVICES("Geräte"),
    SYSTEM("System"),
    BACKUP("Backup")
}

@Composable
private fun SystemHelpSection(onOpenHelp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hilfe", style = MaterialTheme.typography.titleMedium)
        Text("App-Informationen, Version und Link zum GitHub-Projekt anzeigen.")
        StandardActionButton(
            text = "Hilfe anzeigen",
            onClick = onOpenHelp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DisplaySettingsSection(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 36.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text("Darstellung", style = MaterialTheme.typography.titleMedium)
            AppThemeMode.entries.forEach { option ->
                RadioOptionRow(
                    label = when (option) {
                        AppThemeMode.SYSTEM -> "Systemvorgabe"
                        AppThemeMode.LIGHT -> "Hell"
                        AppThemeMode.DARK -> "Dunkel"
                    },
                    selected = themeMode == option,
                    onClick = { onThemeModeChange(option) }
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
            Text("Aktionsdetails", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clickable { onShowActionDetailsChange(!showActionDetails) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aktionsdetails anzeigen")
                Switch(
                    checked = showActionDetails,
                    onCheckedChange = onShowActionDetailsChange
                )
            }

            Text("Höhe des Detailbereichs", style = MaterialTheme.typography.titleSmall)
            DetailPanelHeight.entries.forEach { option ->
                RadioOptionRow(
                    label = when (option) {
                        DetailPanelHeight.TWENTY_PERCENT -> "20 %"
                        DetailPanelHeight.THIRTY_PERCENT -> "30 %"
                        DetailPanelHeight.FORTY_PERCENT -> "40 %"
                    },
                    selected = detailPanelHeight == option,
                    onClick = { onDetailPanelHeightChange(option) },
                    enabled = showActionDetails
                )
            }

            Text(
                text = "Sortierung Aktionsdetails",
                color = if (showActionDetails) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                style = MaterialTheme.typography.titleSmall
            )
            RadioOptionRow(
                label = "Neueste oben",
                selected = diagnosticsNewestFirst,
                onClick = { onDiagnosticsNewestFirstChange(true) },
                enabled = showActionDetails
            )
            RadioOptionRow(
                label = "Neueste unten",
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
                text = "Name\nSSID",
                style = MaterialTheme.typography.titleSmall
            )

            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "WLAN-Profil hinzufügen"
                )
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
                    contentDescription = "Weitere WLAN-Profile oberhalb",
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
                    contentDescription = "Weitere WLAN-Profile unterhalb",
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
            text = "Keine WLAN-Profile konfiguriert.",
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
            title = "WLAN-Profil löschen",
            onDismissRequest = { pendingDeleteProfile = null },
            actionText = "Ja",
            onAction = {
                pendingDeleteProfile = null
                onDeleteClick()
            },
            cancelText = "Nein"
        ) {
            Text("WLAN-Profil ${profileToDelete.name} wirklich löschen?")
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
                    text = "SSID: ${profile.ssid}",
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
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    StandardConfigurationDialog(
        title = if (isNewProfile) "WLAN-Profil anlegen" else "WLAN-Profil bearbeiten",
        onDismissRequest = onCancelClick,
        actionText = "Speichern",
        onAction = onSaveClick
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
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = ssid,
            onValueChange = onSsidChange,
            label = { Text("SSID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Passwort") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
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
                        contentDescription = "Passwort anzeigen oder verbergen"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Leeres Passwort ist erlaubt. Vorhandenes Passwort bleibt nur erhalten, wenn die Sternchen unverändert bleiben.",
            style = MaterialTheme.typography.bodySmall
        )

        StandardActionButton(
            text = "Passwort leeren",
            onClick = onClearPasswordClick,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let { message ->
            Text(
                text = message,
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
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Konfigurationen als JSON-Datei sichern oder aus einer vertrauenswürdigen Quelle importieren.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (isTransferInProgress) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text("Konfiguration wird verarbeitet …")
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Export", style = MaterialTheme.typography.titleSmall)
                StandardActionButton(
                    text = "Exportieren",
                    onClick = onExportClick,
                    modifier = Modifier.fillMaxWidth()
                )
                StandardActionButton(
                    text = "Exportieren mit Passwörtern",
                    onClick = onExportWithPasswordsClick,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Import", style = MaterialTheme.typography.titleSmall)
                StandardActionButton(
                    text = "Datei importieren",
                    onClick = onImportFileClick,
                    modifier = Modifier.fillMaxWidth()
                )
                StandardActionButton(
                    text = "URL importieren",
                    onClick = onImportUrlClick,
                    modifier = Modifier.fillMaxWidth()
                )
                StandardActionButton(
                    text = "QR-Code importieren",
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
        title = "Passwörter unverschlüsselt exportieren?",
        onDismissRequest = onCancel,
        actionText = "Passwörter exportieren",
        onAction = onExport
    ) {
        Text(
            "Die Exportdatei enthält WLAN-Passwörter im Klartext. " +
                "Teile sie nur mit Personen, die diese Passwörter kennen dürfen."
        )
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
        title = "Importmodus wählen",
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
    var url by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(ConfigurationImportMode.MERGE) }
    StandardConfigurationDialog(
        title = "Aus HTTPS-URL importieren",
        onDismissRequest = onCancel,
        actionText = "Import prüfen",
        onAction = { onImport(url, mode) },
        actionEnabled = url.isNotBlank()
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("HTTPS-URL") },
            singleLine = true,
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
            title = "Ergänzen / überschreiben",
            description = "Bestehende Einträge bleiben erhalten. Gleiche IDs werden überschrieben.",
            onClick = { onModeChange(ConfigurationImportMode.MERGE) }
        )
        ImportModeOption(
            selected = mode == ConfigurationImportMode.REPLACE,
            title = "Alles ersetzen",
            description = "Alle lokalen Geräte, WLAN-Profile und Passwörter werden zuerst gelöscht.",
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
    val text = buildString {
        appendLine("WLAN-Profile: ${summary.wifiProfilesNew} neu, ${summary.wifiProfilesOverwritten} überschrieben")
        appendLine("Geräte: ${summary.devicesNew} neu, ${summary.devicesOverwritten} überschrieben")
        appendLine("Passwörter: ${summary.passwordsIncluded} enthalten, ${summary.passwordsDeleted} werden gelöscht")
        if (mode == ConfigurationImportMode.REPLACE) {
            appendLine()
            appendLine("${summary.localWifiProfilesDeleted} lokale WLAN-Profile werden gelöscht.")
            append("${summary.localDevicesDeleted} lokale Geräte werden gelöscht.")
        }
    }
    StandardConfigurationDialog(
        title = "Import-Zusammenfassung",
        onDismissRequest = onCancel,
        actionText = "Importieren",
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
        title = "Import enthält Passwörter",
        onDismissRequest = onCancel,
        actionText = "Importieren",
        onAction = onImport
    ) {
        Text(
            "Die Importdatei enthält WLAN-Passwörter im Klartext oder löscht gespeicherte Passwörter. " +
                "Importiere sie nur aus einer vertrauenswürdigen Quelle."
        )
    }
}

private const val EXPORT_FILE_NAME = "switchwerk-config.json"
