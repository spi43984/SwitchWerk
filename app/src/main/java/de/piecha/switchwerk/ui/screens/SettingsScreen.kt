package de.piecha.switchwerk.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.isEditingWifiProfile) {
        viewModel.cancelWifiProfileEdit()
    }

    BackHandler(enabled = !uiState.isEditingWifiProfile) {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
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

        SettingsSection(
            title = "Geräte",
            description = "Gerätenamen, Aktionsbuttons, API-Aufrufe und WLAN-Zuordnungen werden später hier verwaltet."
        )

        SettingsSection(
            title = "Import / Export",
            description = "Konfigurationen können später ohne WLAN-Passwörter exportiert und importiert werden."
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateBack
        ) {
            Text("Zurück zum Dashboard")
        }
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
private fun SettingsSection(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
