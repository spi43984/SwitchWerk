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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import de.piecha.switchwerk.domain.model.AppUpdateDownloadState
import de.piecha.switchwerk.domain.model.AppUpdateError
import de.piecha.switchwerk.domain.model.AppUpdateSnapshot
import de.piecha.switchwerk.ui.components.SettingsSectionTabs
import de.piecha.switchwerk.ui.components.InfoHint
import de.piecha.switchwerk.ui.components.LazyListScrollIndicator
import de.piecha.switchwerk.ui.components.StandardActionButton
import de.piecha.switchwerk.ui.components.StandardConfigurationDialog
import de.piecha.switchwerk.ui.components.SwipeToDeleteListItem
import de.piecha.switchwerk.ui.components.VerticalScrollIndicator
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.ui.asString
import java.text.DateFormat
import java.util.Date
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    selectedSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit,
    onNavigateBack: () -> Unit,
    onShowSetupWizard: () -> Unit,
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
    var importPasswordChoice by remember { mutableStateOf(initialUiState.importPasswordChoice) }
    var importModeTouched by remember { mutableStateOf(false) }
    var showPasswordExportWarning by remember { mutableStateOf(initialUiState.showPasswordExportWarning) }
    var exportPasswordChoice by remember { mutableStateOf(initialUiState.exportPasswordChoice) }
    var pendingExportIncludesPasswords by remember { mutableStateOf(false) }
    var openSwipeItemId by remember { mutableStateOf(initialUiState.openSwipeItemId) }
    var pendingImportConfirmation by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.installEvents.collect { intent ->
            runCatching { context.startActivity(intent) }
                .onFailure { viewModel.reportUpdateInstallFailed() }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.releasePageEvents.collect { intent ->
            runCatching { context.startActivity(intent) }
                .onFailure { viewModel.reportReleasePageOpenFailed() }
        }
    }

    SideEffect {
        onUiStateChanged(
            SettingsScreenUiState(
                importMode = importMode,
                urlImportValue = urlImportValue,
                fileImportUri = fileImportUri,
                fileImportReference = fileImportReference,
                showImportConfigurationDialog = showImportConfigurationDialog,
                importSource = importSource,
                importPasswordChoice = importPasswordChoice,
                showPasswordExportWarning = showPasswordExportWarning,
                exportPasswordChoice = exportPasswordChoice,
                openSwipeItemId = openSwipeItemId
            )
        )
    }

    val exportConfigurationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val includePasswords = pendingExportIncludesPasswords
        pendingExportIncludesPasswords = false
        exportPasswordChoice = PasswordTransferChoice.UNDECIDED
        uri?.let { viewModel.exportConfiguration(it, includePasswords = includePasswords) }
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
    fun launchConfigurationExport(includePasswords: Boolean) {
        pendingExportIncludesPasswords = includePasswords
        exportPasswordChoice = PasswordTransferChoice.UNDECIDED
        exportConfigurationLauncher.launch(EXPORT_FILE_NAME)
    }
    fun resetImportDialogState() {
        importSource = null
        urlImportValue = ""
        fileImportUri = ""
        fileImportReference = ""
        importPasswordChoice = PasswordTransferChoice.UNDECIDED
        importModeTouched = false
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            importPasswordChoice = PasswordTransferChoice.UNDECIDED
            exportPasswordChoice = PasswordTransferChoice.UNDECIDED
        }
    }

    BackHandler(enabled = uiState.isEditingWifiProfile) {
        viewModel.cancelWifiProfileEdit()
    }

    BackHandler(enabled = uiState.isEditingDevice) {
        viewModel.cancelDeviceEdit()
    }

    BackHandler(enabled = uiState.isEditingSwitchGroup) {
        viewModel.cancelSwitchGroupEdit()
    }

    BackHandler(
        enabled = !uiState.isEditingWifiProfile && !uiState.isEditingDevice &&
            !uiState.isEditingSwitchGroup
    ) {
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
                    onShortcutEnabledChange = viewModel::updateDeviceShortcutEnabled,
                    onApiProtocolChange = viewModel::updateDeviceApiProtocol,
                    onApiMethodChange = viewModel::updateDeviceApiMethod,
                    onApiPathChange = viewModel::updateDeviceApiPath,
                    onApiRequestBodyChange = viewModel::updateDeviceApiRequestBody,
                    onApiContentTypeChange = viewModel::updateDeviceApiContentType,
                    onAddConnection = viewModel::addDeviceConnection,
                    onUpdateConnection = viewModel::updateDeviceConnection,
                    onDeleteConnection = viewModel::deleteDeviceConnection,
                    onMoveConnection = viewModel::moveDeviceConnection,
                    onSaveClick = viewModel::saveDevice,
                    onCancelClick = viewModel::cancelDeviceEdit,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsSection.GROUPS -> SwitchGroupManagementSection(
                    groups = uiState.switchGroups,
                    devices = uiState.devices,
                    isEditing = uiState.isEditingSwitchGroup,
                    form = uiState.switchGroupForm,
                    openSwipeItemId = openSwipeItemId,
                    onOpenSwipeItem = { openSwipeItemId = it },
                    onCloseSwipeItem = { openSwipeItemId = null },
                    onAddClick = { runAfterClosingSwipe(viewModel::startNewSwitchGroup) },
                    onEditClick = viewModel::startEditSwitchGroup,
                    onDeleteClick = viewModel::deleteSwitchGroup,
                    onNameChange = viewModel::updateSwitchGroupName,
                    onActionLabelChange = viewModel::updateSwitchGroupActionLabel,
                    onErrorStrategyChange = viewModel::updateSwitchGroupErrorStrategy,
                    onAddMember = viewModel::addSwitchGroupMember,
                    onDeleteMember = viewModel::deleteSwitchGroupMember,
                    onMoveMember = viewModel::moveSwitchGroupMember,
                    onMemberPauseChange = viewModel::updateSwitchGroupMemberPause,
                    onSaveClick = viewModel::saveSwitchGroup,
                    onCancelClick = viewModel::cancelSwitchGroupEdit,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsSection.SYSTEM -> {
                    val sectionScrollState = rememberScrollState()
                    var sectionViewportHeight by remember { mutableIntStateOf(0) }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { sectionViewportHeight = it.height }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(sectionScrollState)
                                .padding(end = 8.dp),
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
                            UpdateSettingsSection(
                                snapshot = uiState.updateSnapshot,
                                downloadState = uiState.updateDownloadState,
                                isChecking = uiState.isUpdateCheckInProgress,
                                onCheckClick = viewModel::checkForUpdatesManually,
                                onDownloadClick = viewModel::downloadUpdate,
                                onInstallClick = viewModel::installDownloadedUpdate,
                                onOpenReleasePageClick = viewModel::openAvailableReleasePage
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
                            ExternalIntentsSettingsSection(
                                enabled = uiState.appSettings.externalIntentsEnabled,
                                onEnabledChange = viewModel::setExternalIntentsEnabled
                            )
                            HorizontalDivider()
                            SystemSetupWizardSection(
                                onShowSetupWizard = {
                                    viewModel.showSetupWizardAgain()
                                    onShowSetupWizard()
                                }
                            )
                        }
                        VerticalScrollIndicator(
                            scrollState = sectionScrollState,
                            viewportHeight = sectionViewportHeight,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                SettingsSection.BACKUP -> {
                    val sectionScrollState = rememberScrollState()
                    var sectionViewportHeight by remember { mutableIntStateOf(0) }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { sectionViewportHeight = it.height }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(sectionScrollState)
                                .padding(end = 8.dp)
                        ) {
                            ImportExportSection(
                                isTransferInProgress = uiState.isTransferInProgress,
                                passwordChoice = exportPasswordChoice,
                                onPasswordChoiceChange = { exportPasswordChoice = it },
                                onExportClick = {
                                    runAfterClosingSwipe {
                                        viewModel.clearStatusMessage()
                                        if (exportPasswordChoice == PasswordTransferChoice.WITH_PASSWORDS) {
                                            showPasswordExportWarning = true
                                        } else {
                                            launchConfigurationExport(includePasswords = false)
                                        }
                                    }
                                },
                                onImportClick = {
                                    runAfterClosingSwipe {
                                        resetImportDialogState()
                                        viewModel.cancelPendingImport()
                                        showImportConfigurationDialog = true
                                    }
                                }
                            )
                        }
                        VerticalScrollIndicator(
                            scrollState = sectionScrollState,
                            viewportHeight = sectionViewportHeight,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }
    }

    if (showPasswordExportWarning) {
        PasswordExportWarningDialog(
            onExport = {
                showPasswordExportWarning = false
                launchConfigurationExport(includePasswords = true)
            },
            onCancel = {
                showPasswordExportWarning = false
                exportPasswordChoice = PasswordTransferChoice.UNDECIDED
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
            errorMessage = uiState.errorMessage,
            mode = uiState.importMode ?: importMode,
            importPasswordChoice = importPasswordChoice,
            importModeTouched = importModeTouched,
            onSelectUrl = {
                importSource = ImportSource.URL
                fileImportUri = ""
                fileImportReference = ""
                importPasswordChoice = PasswordTransferChoice.UNDECIDED
                importModeTouched = false
                viewModel.cancelPendingImport()
            },
            onPickFile = {
                importSource = ImportSource.FILE
                urlImportValue = ""
                fileImportUri = ""
                fileImportReference = ""
                importPasswordChoice = PasswordTransferChoice.UNDECIDED
                importModeTouched = false
                viewModel.cancelPendingImport()
                importFileLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
            },
            onScanQrCode = {
                importSource = ImportSource.QR_CODE
                importPasswordChoice = PasswordTransferChoice.UNDECIDED
                importModeTouched = false
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
                importModeTouched = true
                viewModel.updateImportMode(mode)
            },
            onImportPasswordChoiceChange = { importPasswordChoice = it },
            onImport = { summary ->
                val includePasswords = importPasswordChoice == PasswordTransferChoice.WITH_PASSWORDS
                showImportConfigurationDialog = false
                resetImportDialogState()
                confirmImportWithOptionalScanPermission(
                    importsWifiProfiles =
                        summary.wifiProfilesNew + summary.wifiProfilesOverwritten > 0,
                    confirmImport = { viewModel.confirmImport(includePasswords) }
                )
            },
            onCancel = {
                showImportConfigurationDialog = false
                resetImportDialogState()
                viewModel.cancelPendingImport()
            }
        )
    }
}

@Composable
private fun ExternalIntentsSettingsSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 36.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.external_intents_title),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clickable { onEnabledChange(!enabled) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.external_intents_enable))
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }
            Text(
                stringResource(R.string.external_intents_description),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SystemSetupWizardSection(onShowSetupWizard: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.setup_wizard_system_title), style = MaterialTheme.typography.titleMedium)
        StandardActionButton(
            text = stringResource(R.string.setup_wizard_show_again),
            onClick = onShowSetupWizard,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UpdateSettingsSection(
    snapshot: AppUpdateSnapshot?,
    downloadState: AppUpdateDownloadState,
    isChecking: Boolean,
    onCheckClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit,
    onOpenReleasePageClick: () -> Unit
) {
    val release = snapshot?.availableRelease
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.updates), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.installed_version_value, snapshot?.installedVersion.orEmpty()))
        Text(
            text = if (release == null) {
                stringResource(R.string.available_version_none)
            } else {
                stringResource(R.string.available_version_value, release.version)
            }
        )
        snapshot?.lastCheckedAtMillis?.let {
            Text(
                stringResource(
                    R.string.update_last_checked_value,
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date(it))
                )
            )
        }
        when {
            snapshot?.isDebugBuild == true -> Text(stringResource(R.string.update_debug_build_hint))
            snapshot?.isUpdateAvailable == true -> Text(stringResource(R.string.update_available_hint))
            release != null -> Text(stringResource(R.string.update_current_hint))
        }
        snapshot?.error?.let { error ->
            Text(
                text = stringResource(error.messageResourceId()),
                color = MaterialTheme.colorScheme.error
            )
        }
        when (downloadState) {
            AppUpdateDownloadState.Idle -> Unit
            AppUpdateDownloadState.Started -> {
                Text(stringResource(R.string.update_download_started))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            is AppUpdateDownloadState.Progress -> {
                Text(stringResource(R.string.update_download_progress, downloadState.percent))
                LinearProgressIndicator(
                    progress = { downloadState.percent / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is AppUpdateDownloadState.Completed -> {
                Text(stringResource(R.string.update_download_completed))
            }
            is AppUpdateDownloadState.Failed -> {
                Text(
                    text = stringResource(R.string.update_download_failed),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        StandardActionButton(
            text = if (isChecking) {
                stringResource(R.string.update_checking)
            } else {
                stringResource(R.string.check_for_updates)
            },
            onClick = onCheckClick,
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth()
        )
        if (!release?.htmlUrl.isNullOrBlank()) {
            StandardActionButton(
                text = stringResource(R.string.open_github_release),
                onClick = onOpenReleasePageClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StandardActionButton(
                text = stringResource(R.string.download_update),
                onClick = onDownloadClick,
                enabled = snapshot?.isUpdateAvailable == true &&
                    downloadState !is AppUpdateDownloadState.Started &&
                    downloadState !is AppUpdateDownloadState.Progress,
                modifier = Modifier.weight(1f)
            )
            StandardActionButton(
                text = stringResource(R.string.install_update),
                onClick = onInstallClick,
                enabled = snapshot?.isUpdateAvailable == true &&
                    (downloadState is AppUpdateDownloadState.Completed || snapshot.downloadedApkUri != null),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun AppUpdateError.messageResourceId(): Int = when (this) {
    AppUpdateError.DebugBuild -> R.string.update_error_debug_build
    AppUpdateError.NoRegularRelease -> R.string.update_error_no_regular_release
    AppUpdateError.MissingApkAsset -> R.string.update_error_missing_apk
    AppUpdateError.AmbiguousApkAsset -> R.string.update_error_ambiguous_apk
    AppUpdateError.InvalidReleaseData -> R.string.update_error_invalid_release
    AppUpdateError.Network -> R.string.update_error_network
    AppUpdateError.GitHub -> R.string.update_error_github
    AppUpdateError.Download -> R.string.update_error_download
    AppUpdateError.Install -> R.string.update_error_install
}

data class SettingsScreenUiState(
    val importMode: ConfigurationImportMode = ConfigurationImportMode.MERGE,
    val urlImportValue: String = "",
    val fileImportUri: String = "",
    val fileImportReference: String = "",
    val showImportConfigurationDialog: Boolean = false,
    val importSource: ImportSource? = null,
    val importPasswordChoice: PasswordTransferChoice = PasswordTransferChoice.UNDECIDED,
    val showPasswordExportWarning: Boolean = false,
    val exportPasswordChoice: PasswordTransferChoice = PasswordTransferChoice.UNDECIDED,
    val openSwipeItemId: String? = null
)

enum class PasswordTransferChoice {
    WITHOUT_PASSWORDS,
    UNDECIDED,
    WITH_PASSWORDS
}

enum class ImportSource {
    FILE,
    URL,
    QR_CODE
}

enum class SettingsSection(val titleResourceId: Int) {
    WIFI_PROFILES(R.string.settings_tab_wifi_profiles),
    DEVICES(R.string.settings_tab_devices),
    GROUPS(R.string.settings_tab_groups),
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
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp)
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
        LazyListScrollIndicator(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
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
    passwordChoice: PasswordTransferChoice,
    onPasswordChoiceChange: (PasswordTransferChoice) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.export), style = MaterialTheme.typography.titleMedium)
                    PasswordChoiceSelector(
                        choice = passwordChoice,
                        onChoiceChange = onPasswordChoiceChange
                    )
                    StandardActionButton(
                        text = stringResource(R.string.export_configuration),
                        onClick = onExportClick,
                        enabled = passwordChoice != PasswordTransferChoice.UNDECIDED,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.import_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.import_section_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    StandardActionButton(
                        text = stringResource(R.string.import_configuration),
                        onClick = onImportClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordChoiceSelector(
    title: String? = null,
    choice: PasswordTransferChoice,
    onChoiceChange: (PasswordTransferChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        title?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PasswordChoiceOption(
                text = stringResource(R.string.password_choice_without),
                selected = choice == PasswordTransferChoice.WITHOUT_PASSWORDS,
                onClick = { onChoiceChange(PasswordTransferChoice.WITHOUT_PASSWORDS) },
                modifier = Modifier.weight(1f)
            )
            PasswordChoiceOption(
                text = stringResource(R.string.password_choice_undecided),
                selected = choice == PasswordTransferChoice.UNDECIDED,
                onClick = { onChoiceChange(PasswordTransferChoice.UNDECIDED) },
                modifier = Modifier.weight(1f)
            )
            PasswordChoiceOption(
                text = stringResource(R.string.password_choice_with),
                selected = choice == PasswordTransferChoice.WITH_PASSWORDS,
                onClick = { onChoiceChange(PasswordTransferChoice.WITH_PASSWORDS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PasswordChoiceOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
        summary.externalIntentsEnabledChange?.let { enabled ->
            ImportSummaryRow(
                stringResource(R.string.import_summary_external_intents_label),
                stringResource(
                    if (enabled) {
                        R.string.import_summary_external_intents_enabled
                    } else {
                        R.string.import_summary_external_intents_disabled
                    }
                )
            )
        }
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
    errorMessage: UiText?,
    mode: ConfigurationImportMode,
    importPasswordChoice: PasswordTransferChoice,
    importModeTouched: Boolean,
    onSelectUrl: () -> Unit,
    onPickFile: () -> Unit,
    onScanQrCode: () -> Unit,
    onUrlChange: (String) -> Unit,
    onLoadUrl: () -> Unit,
    onLoadFile: () -> Unit,
    onModeChange: (ConfigurationImportMode) -> Unit,
    onImportPasswordChoiceChange: (PasswordTransferChoice) -> Unit,
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
    val highlightedStep = when {
        source == null -> ImportDialogStep.SOURCE
        summary == null && !isPreparing && errorMessage == null -> ImportDialogStep.LOAD
        summary != null && importPasswordChoice != PasswordTransferChoice.UNDECIDED -> ImportDialogStep.ACTIONS
        summary != null && !importModeTouched -> ImportDialogStep.OPTIONS
        summary != null -> ImportDialogStep.PASSWORDS
        else -> null
    }
    StandardConfigurationDialog(
        title = stringResource(R.string.import_configuration),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.import_action),
        onAction = { summary?.let(onImport) },
        actionEnabled = summary != null &&
            errorMessage == null &&
            !isPreparing &&
            importPasswordChoice != PasswordTransferChoice.UNDECIDED,
        scrollToBottom = highlightedStep == ImportDialogStep.ACTIONS,
        scrollToBottomSignal = importPasswordChoice.ordinal
    ) {
        HighlightedImportStep(active = highlightedStep == ImportDialogStep.SOURCE) {
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
        }
        if (source == ImportSource.URL) {
            HighlightedImportStep(active = highlightedStep == ImportDialogStep.LOAD) {
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
            }
        } else if (source == ImportSource.FILE) {
            HighlightedImportStep(active = highlightedStep == ImportDialogStep.LOAD) {
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
        }

        HorizontalDivider()
        HighlightedImportStep(active = highlightedStep == ImportDialogStep.OPTIONS) {
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
        }
        HighlightedImportStep(active = highlightedStep == ImportDialogStep.PASSWORDS) {
            Text(stringResource(R.string.import_step_passwords), style = MaterialTheme.typography.titleSmall)
            PasswordChoiceSelector(
                choice = importPasswordChoice,
                onChoiceChange = onImportPasswordChoiceChange
            )
            Text(
                text = stringResource(
                    when {
                        !containsPasswordFields -> R.string.import_passwords_no_fields_hint
                        importPasswordChoice == PasswordTransferChoice.WITH_PASSWORDS ->
                            R.string.import_passwords_enabled_hint
                        importPasswordChoice == PasswordTransferChoice.WITHOUT_PASSWORDS ->
                            R.string.import_passwords_disabled_hint
                        else -> R.string.import_passwords_required_hint
                    }
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }

        HorizontalDivider()
        HighlightedImportStep(active = highlightedStep == ImportDialogStep.ACTIONS) {
            Text(stringResource(R.string.import_step_summary), style = MaterialTheme.typography.titleSmall)
            if (isPreparing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(stringResource(R.string.configuration_processing))
                }
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (summary == null) {
                Text(stringResource(R.string.import_select_source_hint))
            } else {
                ImportSummaryContent(
                    summary = summary,
                    mode = mode,
                    importPasswords = importPasswordChoice == PasswordTransferChoice.WITH_PASSWORDS
                )
                Text(
                    text = stringResource(R.string.import_next_actions_hint),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private enum class ImportDialogStep {
    SOURCE,
    LOAD,
    OPTIONS,
    PASSWORDS,
    ACTIONS
}

@Composable
private fun HighlightedImportStep(
    active: Boolean,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "import_step_highlight")
    val activeAlpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "import_step_highlight_alpha"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (active) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = activeAlpha)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (active) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = activeAlpha))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
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
