package de.piecha.switchwerk.data.transfer

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
}
