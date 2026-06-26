package de.piecha.switchwerk.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.components.AboutContent

private val AboutScreenPadding = 24.dp
private val AboutContentPadding = 16.dp
private const val AboutIconWidthFraction = 0.85f
private val AboutIconMaxWidth = 435.dp

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val versionName = context.packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName
        .orEmpty()
    val portraitContentWidth = (
        configuration.smallestScreenWidthDp.dp -
            AboutScreenPadding * 2 -
            AboutContentPadding * 2
        ).coerceAtLeast(0.dp)
    val aboutIconMaxWidth = minOf(
        portraitContentWidth * AboutIconWidthFraction,
        AboutIconMaxWidth
    )

    BackHandler(onBack = onNavigateBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(AboutScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(stringResource(R.string.about_switchwerk), style = MaterialTheme.typography.headlineLarge)
        }
        AboutContent(versionName, iconMaxWidth = aboutIconMaxWidth)
    }
}
