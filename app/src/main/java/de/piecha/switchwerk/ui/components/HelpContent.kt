package de.piecha.switchwerk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import de.piecha.switchwerk.R

@Composable
fun HelpContent(
    modifier: Modifier = Modifier,
    onShowSetupWizard: () -> Unit = {}
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HelpSection(R.string.help_getting_started_title, R.string.help_getting_started_text)
            HelpSection(R.string.help_connection_title, R.string.help_connection_text)
            HelpSection(R.string.help_import_title, R.string.help_import_text)
            HelpSection(R.string.help_privacy_title, R.string.help_privacy_text)
            StandardActionButton(
                text = stringResource(R.string.setup_wizard_show_again),
                onClick = onShowSetupWizard,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HelpSection(titleResourceId: Int, textResourceId: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(titleResourceId), style = MaterialTheme.typography.titleSmall)
        BulletListText(stringResource(textResourceId))
    }
}

@Composable
fun AboutContent(
    versionName: String,
    modifier: Modifier = Modifier,
    iconMaxWidth: Dp = 435.dp
) {
    val uriHandler = LocalUriHandler.current

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val iconWidth = minOf(maxWidth * 0.85f, iconMaxWidth)

                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .width(iconWidth)
                        .aspectRatio(988f / 1050f),
                    contentScale = ContentScale.Fit
                )
            }
            Text(stringResource(R.string.about_description))
            Text(stringResource(R.string.version_value, versionName))
            Text(stringResource(R.string.release_date))
            Text(stringResource(R.string.about_author))
            Text(stringResource(R.string.about_license))
            StandardActionButton(
                text = stringResource(R.string.open_github_project),
                onClick = { uriHandler.openUri(PROJECT_URL) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private const val PROJECT_URL = "https://github.com/spi43984/SwitchWerk"
