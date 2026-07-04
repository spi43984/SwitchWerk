package de.piecha.switchwerk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.piecha.switchwerk.R

@Composable
fun SetupWizard(
    initialScrollPosition: Int,
    onOpenHelp: (Int) -> Unit,
    onOpenBackup: (Int) -> Unit,
    onOpenWifiProfiles: (Int) -> Unit,
    onOpenDevices: (Int) -> Unit,
    onOpenDashboard: (Int) -> Unit,
    onSkip: () -> Unit,
    onDoNotShowAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState(initial = initialScrollPosition)
    var scrollViewportHeight by remember { mutableIntStateOf(0) }

    LaunchedEffect(initialScrollPosition) {
        scrollState.scrollTo(initialScrollPosition)
    }

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = modifier
                    .padding(12.dp)
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * WIZARD_HEIGHT_FRACTION)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setup_wizard_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.setup_wizard_return_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .onSizeChanged { scrollViewportHeight = it.height }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(end = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WizardBlock(
                                title = stringResource(R.string.setup_wizard_intro_title)
                            ) {
                                Text(stringResource(R.string.setup_wizard_intro_text))
                                StandardActionButton(
                                    text = stringResource(R.string.show_help),
                                    onClick = { onOpenHelp(scrollState.value) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            WizardBlock(
                                title = stringResource(R.string.setup_wizard_import_title)
                            ) {
                                Text(stringResource(R.string.setup_wizard_import_text))
                                StandardActionButton(
                                    text = stringResource(R.string.setup_wizard_open_backup),
                                    onClick = { onOpenBackup(scrollState.value) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            WizardBlock(
                                title = stringResource(R.string.setup_wizard_manual_title)
                            ) {
                                Text(stringResource(R.string.setup_wizard_manual_text))
                                StandardActionButton(
                                    text = stringResource(R.string.setup_wizard_open_wifi_profiles),
                                    onClick = { onOpenWifiProfiles(scrollState.value) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                StandardActionButton(
                                    text = stringResource(R.string.setup_wizard_open_devices),
                                    onClick = { onOpenDevices(scrollState.value) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                StandardActionButton(
                                    text = stringResource(R.string.setup_wizard_open_dashboard),
                                    onClick = { onOpenDashboard(scrollState.value) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        VerticalScrollIndicator(
                            scrollState = scrollState,
                            viewportHeight = scrollViewportHeight,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandardActionButton(
                            text = stringResource(R.string.setup_wizard_skip),
                            onClick = onSkip,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        StandardActionButton(
                            text = stringResource(R.string.setup_wizard_do_not_show_again),
                            onClick = onDoNotShowAgain,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardBlock(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            content()
        }
    }
}

private const val WIZARD_HEIGHT_FRACTION = 0.96f
