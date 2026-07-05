package de.piecha.switchwerk.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun CoroutineScope.launchWidgetUpdate(block: suspend () -> Unit) {
    launch(Dispatchers.IO) {
        runCatching { block() }
    }
}
