package de.piecha.switchwerk.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.components.AboutContent
import de.piecha.switchwerk.ui.components.StandardDialogButtons
import de.piecha.switchwerk.ui.components.VerticalScrollIndicator

private val AboutScreenPadding = 24.dp
private val AboutContentPadding = 16.dp
private const val AboutIconWidthFraction = 0.85f
private val AboutIconMaxWidth = 435.dp

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
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
    val scrollState = rememberScrollState()
    var viewportHeight by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(AboutScreenPadding),
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
        Box(
            modifier = Modifier
                .weight(1f)
                .onSizeChanged { viewportHeight = it.height }
        ) {
            AboutContent(
                versionName = versionName,
                iconMaxWidth = aboutIconMaxWidth,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(end = 8.dp)
            )
            VerticalScrollIndicator(
                scrollState = scrollState,
                viewportHeight = viewportHeight,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        StandardDialogButtons(
            actionText = stringResource(R.string.open_github_project),
            onAction = { uriHandler.openUri(PROJECT_URL) },
            cancelText = stringResource(R.string.close),
            onCancel = onNavigateBack,
            cancelUsesWeight = false
        )
    }
}

private const val PROJECT_URL = "https://github.com/spi43984/SwitchWerk"
