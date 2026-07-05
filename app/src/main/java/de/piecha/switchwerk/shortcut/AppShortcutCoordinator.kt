package de.piecha.switchwerk.shortcut

import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppShortcutCoordinator(
    private val repository: DeviceRepository,
    private val switchGroupRepository: SwitchGroupRepository,
    private val publisher: AppShortcutPublisher,
    private val scope: CoroutineScope
) {
    fun start() {
        scope.launch {
            combine(
                repository.observeDevices(),
                switchGroupRepository.observeSwitchGroups()
            ) { devices, groups ->
                devices to groups
            }.collectLatest { (devices, groups) ->
                publisher.publish(devices, groups)
            }
        }
    }
}
