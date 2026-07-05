package de.piecha.switchwerk.widget

import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest

class WidgetUpdateCoordinator(
    private val deviceRepository: DeviceRepository,
    private val switchGroupRepository: SwitchGroupRepository,
    private val renderer: SwitchWerkWidgetRenderer,
    private val scope: CoroutineScope
) {
    fun start() {
        scope.launchWidgetUpdate {
            combine(
                deviceRepository.observeDevices(),
                switchGroupRepository.observeSwitchGroups()
            ) { _, _ -> Unit }
                .collectLatest {
                    renderer.updateAllWidgets()
                }
        }
    }
}
