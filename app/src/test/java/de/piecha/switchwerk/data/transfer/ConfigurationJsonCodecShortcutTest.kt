package de.piecha.switchwerk.data.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ConfigurationJsonCodecShortcutTest {

    @Test
    fun configurationDeviceWithoutShortcutFieldUsesSafeDefault() {
        val device = ConfigurationDevice(
            id = "device-1",
            name = "Example Device",
            actionLabel = "Switch",
            action = ConfigurationDeviceAction(method = "GET", path = "/rpc/action"),
            connections = emptyList()
        )

        assertFalse(device.shortcutEnabled)
    }

    @Test
    fun configurationSwitchGroupWithoutShortcutFieldUsesSafeDefault() {
        val group = ConfigurationSwitchGroup(
            id = "group-1",
            name = "Example Group",
            actionLabel = "Run",
            members = emptyList()
        )

        assertFalse(group.shortcutEnabled)
        assertEquals("NONE", group.color)
    }

    @Test
    fun configurationDeviceWithoutColorFieldUsesStandardColor() {
        val device = ConfigurationDevice(
            id = "device-1",
            name = "Example Device",
            actionLabel = "Switch",
            action = ConfigurationDeviceAction(method = "GET", path = "/rpc/action"),
            connections = emptyList()
        )

        assertEquals("NONE", device.color)
    }

    @Test
    fun configurationDeviceKeepsSelectedColor() {
        val device = ConfigurationDevice(
            id = "device-1",
            name = "Example Device",
            actionLabel = "Switch",
            action = ConfigurationDeviceAction(method = "GET", path = "/rpc/action"),
            connections = emptyList(),
            color = "BLUE"
        )

        assertEquals("BLUE", device.color)
    }

    @Test
    fun configurationSwitchGroupKeepsSelectedColor() {
        val group = ConfigurationSwitchGroup(
            id = "group-1",
            name = "Example Group",
            actionLabel = "Run",
            color = "PURPLE",
            members = emptyList()
        )

        assertEquals("PURPLE", group.color)
    }
}
