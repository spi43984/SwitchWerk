package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.domain.model.Device

interface DeviceActionService {
    suspend fun execute(device: Device): DeviceActionResult

    suspend fun execute(
        device: Device,
        onDiagnosticEvent: (DeviceActionDiagnosticEvent) -> Unit
    ): DeviceActionResult = execute(device)
}
