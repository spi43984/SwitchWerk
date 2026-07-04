package de.piecha.switchwerk.shortcut

import de.piecha.switchwerk.data.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppShortcutCoordinator(
    private val repository: DeviceRepository,
    private val publisher: AppShortcutPublisher,
    private val scope: CoroutineScope
) {
    fun start() {
        scope.launch {
            repository.observeDevices().collectLatest(publisher::publish)
        }
    }
}
