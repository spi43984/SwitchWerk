package de.piecha.switchwerk.data.transfer

import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurationImportValidatorTest {

    private val validator = ConfigurationImportValidator()

    @Test
    fun validDocumentAcceptsEmptyPasswordAsDeletion() {
        validator.validate(
            validDocument(
                wifiProfiles = listOf(
                    wifiProfile(
                        password = "",
                        isPasswordPresent = true
                    )
                )
            )
        )
    }

    @Test
    fun unsupportedSchemaVersionIsRejected() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument().copy(schemaVersion = 2))
        }

        assertTrue(error.message.orEmpty().contains("schemaVersion"))
    }

    @Test
    fun duplicateWifiProfileIdsAreRejected() {
        val profile = wifiProfile()

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(wifiProfiles = listOf(profile, profile))
            )
        }
    }

    @Test
    fun connectionToUnknownWifiProfileIsRejected() {
        val device = validDocument().devices.single().copy(
            connections = listOf(
                ConfigurationDeviceConnection(
                    wifiProfileId = "unknown",
                    host = "192.168.1.10"
                )
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument(devices = listOf(device)))
        }
    }

    @Test
    fun unsupportedApiMethodIsRejected() {
        val device = validDocument().devices.single().copy(
            action = ConfigurationDeviceAction(
                method = "DELETE",
                path = "/rpc/action"
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument(devices = listOf(device)))
        }
    }

    private fun validDocument(
        wifiProfiles: List<ConfigurationWifiProfile> = listOf(wifiProfile()),
        devices: List<ConfigurationDevice> = listOf(device())
    ): ConfigurationDocument {
        return ConfigurationDocument(
            schemaVersion = CONFIGURATION_SCHEMA_VERSION,
            wifiProfiles = wifiProfiles,
            devices = devices
        )
    }

    private fun wifiProfile(
        password: String? = null,
        isPasswordPresent: Boolean = false
    ): ConfigurationWifiProfile {
        return ConfigurationWifiProfile(
            id = "wifi-1",
            ssid = "Home",
            securityType = "WPA2_PSK",
            password = password,
            isPasswordPresent = isPasswordPresent
        )
    }

    private fun device(): ConfigurationDevice {
        return ConfigurationDevice(
            id = "device-1",
            name = "Light",
            actionLabel = "Switch",
            action = ConfigurationDeviceAction(
                method = "GET",
                path = "/rpc/action"
            ),
            connections = listOf(
                ConfigurationDeviceConnection(
                    wifiProfileId = "wifi-1",
                    host = "192.168.1.10"
                )
            )
        )
    }
}
