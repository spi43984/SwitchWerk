package de.piecha.switchwerk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@Composable
fun HelpContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val versionName = context.packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName
        .orEmpty()

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Über SwitchWerk", style = MaterialTheme.typography.titleMedium)
            Text(
                "SwitchWerk steuert Shelly und andere Geräte direkt im lokalen Netzwerk. " +
                    "Die Konfiguration bleibt auf diesem Gerät."
            )
            Text("Version $versionName")
            StandardActionButton(
                text = "GitHub-Projekt öffnen",
                onClick = { uriHandler.openUri(PROJECT_URL) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private const val PROJECT_URL = "https://github.com/spi43984/SwitchWerk"
