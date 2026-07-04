package de.piecha.switchwerk.shortcut

import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppShortcutTest {

    @Test
    fun selectionUsesEnabledDevicesInDashboardOrderAndHonorsLimit() {
        val devices = listOf(
            device("third", 2, enabled = true),
            device("disabled", 0, enabled = false),
            device("first", 0, enabled = true),
            device("second", 1, enabled = true)
        )

        val shortcuts = selectAppShortcuts(devices, maximumCount = 2)

        assertEquals(listOf("first", "second"), shortcuts.map(AppShortcut::deviceId))
    }

    @Test
    fun intentEvaluationAcceptsOnlyShortcutActionAndOpaqueDeviceId() {
        assertEquals("device-1", shortcutDeviceId(APP_SHORTCUT_ACTION, "device-1"))
        assertNull(shortcutDeviceId("other-action", "device-1"))
        assertNull(shortcutDeviceId(APP_SHORTCUT_ACTION, ""))
        assertNull(shortcutDeviceId(APP_SHORTCUT_ACTION, "x".repeat(129)))
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
}
