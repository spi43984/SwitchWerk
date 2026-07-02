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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.components.HelpContent
import de.piecha.switchwerk.ui.components.StandardDialogButtons
import de.piecha.switchwerk.ui.components.VerticalScrollIndicator

@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    onShowSetupWizard: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)
    val scrollState = rememberScrollState()
    var viewportHeight by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
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
                    contentDescription = stringResource(R.string.back)
                )
            }
            Text(stringResource(R.string.help), style = MaterialTheme.typography.headlineLarge)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .onSizeChanged { viewportHeight = it.height }
        ) {
            HelpContent(
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
            actionText = stringResource(R.string.setup_wizard_show_again),
            onAction = onShowSetupWizard,
            cancelText = stringResource(R.string.close),
            onCancel = onNavigateBack,
            cancelUsesWeight = false
        )
    }
}
