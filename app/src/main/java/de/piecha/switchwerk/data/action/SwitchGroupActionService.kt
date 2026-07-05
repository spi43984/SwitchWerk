package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup

interface SwitchGroupActionService {
    suspend fun execute(
        group: SwitchGroup,
        devices: List<Device>,
        onDiagnosticEvent: (SwitchGroupDiagnosticEvent) -> Unit
    ): SwitchGroupActionResult
}
