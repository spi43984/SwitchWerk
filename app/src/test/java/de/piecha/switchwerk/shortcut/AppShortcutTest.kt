package de.piecha.switchwerk.shortcut

import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.SwitchGroupMember
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppShortcutTest {

    @Test
    fun selectionUsesEnabledDevicesAndGroupsInDashboardOrderAndHonorsLimit() {
        val devices = listOf(
            device("third", 2, enabled = true),
            device("disabled", 0, enabled = false),
            device("first", 0, enabled = true),
            device("second", 1, enabled = true)
        )
        val groups = listOf(
            group("group-first", 0, enabled = true),
            group("empty", 0, enabled = true, memberCount = 0),
            group("group-disabled", 0, enabled = false)
        )

        val shortcuts = selectAppShortcuts(
            devices = devices,
            switchGroups = groups,
            maximumCount = 3
        )

        assertEquals(
            listOf("device:first", "group:group-first", "device:second"),
            shortcuts.map(AppShortcut::shortcutId)
        )
    }

    @Test
    fun intentEvaluationAcceptsOnlyShortcutActionAndOpaqueDeviceId() {
        assertEquals("device-1", shortcutDeviceId(APP_SHORTCUT_ACTION, "device-1"))
        assertNull(shortcutDeviceId("other-action", "device-1"))
        assertNull(shortcutDeviceId(APP_SHORTCUT_ACTION, ""))
        assertNull(shortcutDeviceId(APP_SHORTCUT_ACTION, "x".repeat(129)))
    }

    @Test
    fun intentEvaluationAcceptsOnlyShortcutActionAndOpaqueGroupId() {
        assertEquals("group-1", shortcutGroupId(APP_SHORTCUT_ACTION, "group-1"))
        assertNull(shortcutGroupId("other-action", "group-1"))
        assertNull(shortcutGroupId(APP_SHORTCUT_ACTION, ""))
        assertNull(shortcutGroupId(APP_SHORTCUT_ACTION, "x".repeat(129)))
    }

    private fun device(id: String, sortOrder: Int, enabled: Boolean): Device {
        return Device(
            id = id,
            name = "Device $id",
            actionLabel = "Switch",
            apiCall = ApiCall(ApiMethod.GET, "/rpc/action"),
            connections = emptyList(),
            sortOrder = sortOrder,
            shortcutEnabled = enabled
        )
    }

    private fun group(
        id: String,
        sortOrder: Int,
        enabled: Boolean,
        memberCount: Int = 1
    ): SwitchGroup {
        return SwitchGroup(
            id = id,
            name = "Group $id",
            actionLabel = "Run",
            sortOrder = sortOrder,
            shortcutEnabled = enabled,
            members = List(memberCount) { index ->
                SwitchGroupMember(
                    id = "$id-member-$index",
                    deviceId = "device-$index",
                    pauseAfterMillis = 0L,
                    sortOrder = index
                )
            }
        )
    }
}
