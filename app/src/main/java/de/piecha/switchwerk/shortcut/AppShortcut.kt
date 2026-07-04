package de.piecha.switchwerk.shortcut

import de.piecha.switchwerk.domain.model.Device

const val APP_SHORTCUT_ACTION = "de.piecha.switchwerk.action.RUN_DEVICE"
const val APP_SHORTCUT_DEVICE_ID = "device_id"

data class AppShortcut(
    val deviceId: String,
    val shortLabel: String,
    val longLabel: String
)

fun selectAppShortcuts(devices: List<Device>, maximumCount: Int): List<AppShortcut> {
    if (maximumCount <= 0) return emptyList()
    return devices
        .asSequence()
        .filter(Device::shortcutEnabled)
        .sortedWith(compareBy(Device::sortOrder, Device::id))
        .take(maximumCount)
        .map { device ->
            AppShortcut(
                deviceId = device.id,
                shortLabel = device.name,
                longLabel = "${device.name}: ${device.actionLabel}"
            )
        }
        .toList()
}

fun shortcutDeviceId(action: String?, deviceId: String?): String? {
    if (action != APP_SHORTCUT_ACTION) return null
    return deviceId?.takeIf { it.isNotBlank() && it.length <= 128 }
}
