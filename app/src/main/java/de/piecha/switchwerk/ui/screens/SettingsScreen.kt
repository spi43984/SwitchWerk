package de.piecha.switchwerk.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Einstellungen",
            style = MaterialTheme.typography.headlineLarge
        )

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

        WifiProfileManagementSection(
            profiles = uiState.wifiProfiles,
            isEditing = uiState.isEditingWifiProfile,
            ssid = uiState.form.ssid,
            password = uiState.form.password,
            isPasswordVisible = uiState.form.isPasswordVisible,
            onAddClick = viewModel::startNewWifiProfile,
            onEditClick = viewModel::startEditWifiProfile,
            onDeleteClick = viewModel::deleteWifiProfile,
            onSsidChange = viewModel::updateWifiProfileSsid,
            onPasswordChange = viewModel::updateWifiProfilePassword,
            onClearPasswordClick = viewModel::clearWifiProfilePassword,
            onTogglePasswordVisibility = viewModel::toggleWifiPasswordVisibility,
            onSaveClick = viewModel::saveWifiProfile,
            onCancelClick = viewModel::cancelWifiProfileEdit
        )

        DeviceManagementSection(
            devices = uiState.devices,
            wifiProfiles = uiState.wifiProfiles,
            isEditing = uiState.isEditingDevice,
            form = uiState.deviceForm,
            onAddClick = viewModel::startNewDevice,
            onEditClick = viewModel::startEditDevice,
            onDeleteClick = viewModel::deleteDevice,
            onNameChange = viewModel::updateDeviceName,
            onActionLabelChange = viewModel::updateDeviceActionLabel,
            onApiMethodChange = viewModel::updateDeviceApiMethod,
            onApiPathChange = viewModel::updateDeviceApiPath,
            onAddConnection = viewModel::addDeviceConnection,
            onUpdateConnection = viewModel::updateDeviceConnection,
            onDeleteConnection = viewModel::deleteDeviceConnection,
            onSaveClick = viewModel::saveDevice,
            onCancelClick = viewModel::cancelDeviceEdit
        )

        ImportExportSection(
            isTransferInProgress = uiState.isTransferInProgress,
            onExportClick = {
                viewModel.clearStatusMessage()
                exportWithoutPasswordsLauncher.launch(EXPORT_FILE_NAME)
            },
            onExportWithPasswordsClick = {
                showPasswordExportWarning = true
            },
            onImportFileClick = {
                showFileImportModeDialog = true
            },
            onImportUrlClick = {
                showUrlImportDialog = true
            },
            onScanQrCodeClick = {
                showQrImportModeDialog = true
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateBack
        ) {
            Text("Zurück zum Dashboard")
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
            onImport = viewModel::confirmImportSummary,
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

@Composable
private fun WifiProfileManagementSection(
    profiles: List<WifiProfile>,
    isEditing: Boolean,
    ssid: String,
    password: String,
    isPasswordVisible: Boolean,
    onAddClick: () -> Unit,
    onEditClick: (WifiProfile) -> Unit,
    onDeleteClick: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 6.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WLAN-Profile (SSID)",
                    style = MaterialTheme.typography.titleMedium
                )

                if (!isEditing) {
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
            }

            if (isEditing) {
                WifiProfileForm(
                    ssid = ssid,
                    password = password,
                    isPasswordVisible = isPasswordVisible,
                    onSsidChange = onSsidChange,
                    onPasswordChange = onPasswordChange,
                    onClearPasswordClick = onClearPasswordClick,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    onSaveClick = onSaveClick,
                    onCancelClick = onCancelClick
                )
            } else {
                WifiProfileList(
                    profiles = profiles,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun WifiProfileList(
    profiles: List<WifiProfile>,
    onEditClick: (WifiProfile) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    if (profiles.isEmpty()) {
        Text(
            text = "Keine WLAN-Profile konfiguriert.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.height(172.dp),
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
            modifier = Modifier.height(140.dp)
        ) {
            items(
                items = profiles,
                key = { profile -> profile.id }
            ) { profile ->
                WifiProfileRow(
                    profile = profile,
                    onEditClick = { onEditClick(profile) },
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
private fun WifiProfileRow(
    profile: WifiProfile,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var pendingDeleteProfile by remember { mutableStateOf<WifiProfile?>(null) }

    pendingDeleteProfile?.let { profileToDelete ->
        AlertDialog(
            onDismissRequest = {
                pendingDeleteProfile = null
            },
            title = {
                Text("WLAN-Profil löschen")
            },
            text = {
                Text("SSID ${profileToDelete.ssid} wirklich löschen?")
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        pendingDeleteProfile = null
                    }
                ) {
                    Text("Nein")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        pendingDeleteProfile = null
                        onDeleteClick()
                    }
                ) {
                    Text("Ja")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = profile.ssid,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 4.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "WLAN-Profil bearbeiten"
                )
            }

            IconButton(
                onClick = {
                    pendingDeleteProfile = profile
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "WLAN-Profil löschen"
                )
            }
        }
    }
}

@Composable
private fun WifiProfileForm(
    ssid: String,
    password: String,
    isPasswordVisible: Boolean,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSaveClick
            ) {
                Text("Speichern")
            }

            OutlinedButton(
                onClick = onCancelClick
            ) {
                Text("Abbrechen")
            }

            OutlinedButton(
                onClick = onClearPasswordClick
            ) {
                Text("Passwort leeren")
            }
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Import / Export", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Konfigurationen als JSON-Datei sichern oder aus einer vertrauenswürdigen Quelle importieren.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onExportClick) {
                            Text("Exportieren")
                        }
                        OutlinedButton(onClick = onExportWithPasswordsClick) {
                            Text("Mit Passwörtern")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onImportFileClick) {
                            Text("Datei importieren")
                        }
                        OutlinedButton(onClick = onImportUrlClick) {
                            Text("URL importieren")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onScanQrCodeClick) {
                            Text("QR-Code scannen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordExportWarningDialog(
    onExport: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Passwörter unverschlüsselt exportieren?") },
        text = {
            Text(
                "Die Exportdatei enthält WLAN-Passwörter im Klartext. " +
                    "Teile sie nur mit Personen, die diese Passwörter kennen dürfen."
            )
        },
        confirmButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Abbrechen")
            }
        },
        dismissButton = {
            Button(onClick = onExport) {
                Text("Passwörter exportieren")
            }
        }
    )
}

@Composable
private fun ImportModeDialog(
    continueText: String,
    onContinue: (ConfigurationImportMode) -> Unit,
    onCancel: () -> Unit
) {
    var mode by remember { mutableStateOf(ConfigurationImportMode.MERGE) }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Importmodus wählen") },
        text = {
            ImportModeSelection(
                mode = mode,
                onModeChange = { mode = it }
            )
        },
        confirmButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Abbrechen")
            }
        },
        dismissButton = {
            Button(onClick = { onContinue(mode) }) {
                Text(continueText)
            }
        }
    )
}

@Composable
private fun UrlImportDialog(
    onImport: (String, ConfigurationImportMode) -> Unit,
    onCancel: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(ConfigurationImportMode.MERGE) }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Aus HTTPS-URL importieren") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        },
        confirmButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Abbrechen")
            }
        },
        dismissButton = {
            Button(
                onClick = { onImport(url, mode) },
                enabled = url.isNotBlank()
            ) {
                Text("Import prüfen")
            }
        }
    )
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
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Import-Zusammenfassung") },
        text = { Text(text) },
        confirmButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Abbrechen")
            }
        },
        dismissButton = {
            Button(onClick = onImport) {
                Text("Importieren")
            }
        }
    )
}

@Composable
private fun PasswordImportWarningDialog(
    onImport: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Import enthält Passwörter") },
        text = {
            Text(
                "Die Importdatei enthält WLAN-Passwörter im Klartext oder löscht gespeicherte Passwörter. " +
                    "Importiere sie nur aus einer vertrauenswürdigen Quelle."
            )
        },
        confirmButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Abbrechen")
            }
        },
        dismissButton = {
            Button(onClick = onImport) {
                Text("Importieren")
            }
        }
    )
}

private const val EXPORT_FILE_NAME = "switchwerk-config.json"
