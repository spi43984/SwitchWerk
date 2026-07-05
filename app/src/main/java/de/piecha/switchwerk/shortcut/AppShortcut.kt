package de.piecha.switchwerk.shortcut

import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup

const val APP_SHORTCUT_ACTION = "de.piecha.switchwerk.action.RUN_SHORTCUT"
const val APP_SHORTCUT_DEVICE_ID = "device_id"
const val APP_SHORTCUT_GROUP_ID = "group_id"

sealed interface AppShortcut {
    val shortcutId: String
    val shortLabel: String
    val longLabel: String
    val sortOrder: Int
}

data class DeviceAppShortcut(
    val deviceId: String,
    override val shortLabel: String,
    override val longLabel: String,
    override val sortOrder: Int
) : AppShortcut {
    override val shortcutId: String = "device:$deviceId"
}

data class SwitchGroupAppShortcut(
    val groupId: String,
    override val shortLabel: String,
    override val longLabel: String,
    override val sortOrder: Int
) : AppShortcut {
    override val shortcutId: String = "group:$groupId"
}

fun selectAppShortcuts(
    devices: List<Device>,
    switchGroups: List<SwitchGroup>,
    maximumCount: Int
): List<AppShortcut> {
    if (maximumCount <= 0) return emptyList()
    return (
        devices
            .filter(Device::shortcutEnabled)
            .map { device ->
                DeviceAppShortcut(
                    deviceId = device.id,
                    shortLabel = device.name,
                    longLabel = "${device.name}: ${device.actionLabel}",
                    sortOrder = device.sortOrder
                )
            } +
            switchGroups
                .filter { it.shortcutEnabled && it.members.isNotEmpty() }
                .map { group ->
                    SwitchGroupAppShortcut(
                        groupId = group.id,
                        shortLabel = group.name,
                        longLabel = "${group.name}: ${group.actionLabel}",
                        sortOrder = group.sortOrder
                    )
                }
        )
        .sortedWith(compareBy<AppShortcut> { it.sortOrder }.thenBy { it.shortcutId })
        .take(maximumCount)
}

fun selectAppShortcuts(devices: List<Device>, maximumCount: Int): List<AppShortcut> {
    return selectAppShortcuts(
        devices = devices,
        switchGroups = emptyList(),
        maximumCount = maximumCount
    )
}

fun shortcutDeviceId(action: String?, deviceId: String?): String? {
    if (action != APP_SHORTCUT_ACTION) return null
    return deviceId?.takeIf { it.isNotBlank() && it.length <= 128 }
}

fun shortcutGroupId(action: String?, groupId: String?): String? {
    if (action != APP_SHORTCUT_ACTION) return null
    return groupId?.takeIf { it.isNotBlank() && it.length <= 128 }
}
