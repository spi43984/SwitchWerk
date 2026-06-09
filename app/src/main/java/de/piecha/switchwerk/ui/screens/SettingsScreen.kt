package de.piecha.switchwerk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
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

        Text(
            text = "Konfiguration für WLAN-Profile, Geräte und Import/Export.",
            style = MaterialTheme.typography.bodyLarge
        )

        SettingsSection(
            title = "WLAN-Profile",
            description = "SSID und Zugangsdaten werden später hier verwaltet."
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
