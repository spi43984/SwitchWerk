package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.transfer.ConfigurationDocument
import de.piecha.switchwerk.data.transfer.ConfigurationWifiProfile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreparedConfigurationImportTest {

    @Test
    fun passwordFieldIsRecognized() {
        val preparedImport = preparedImport(
            ConfigurationDocument(
                schemaVersion = 2,
                wifiProfiles = listOf(
                    ConfigurationWifiProfile(
                        id = "wifi-1",
                        name = "Home",
                        ssid = "Home",
                        securityType = "WPA2_PSK",
                        password = "secret",
                        isPasswordPresent = true
                    )
                ),
                devices = emptyList()
            )
        )

        assertTrue(preparedImport.containsPasswordChanges)
    }

    @Test
    fun configurationWithoutPasswordDataNeedsNoWarning() {
        val preparedImport = preparedImport(
            ConfigurationDocument(
                schemaVersion = 2,
                wifiProfiles = emptyList(),
                devices = emptyList()
            )
        )

        assertFalse(preparedImport.containsPasswordChanges)
    }

    private fun preparedImport(document: ConfigurationDocument): PreparedConfigurationImport {
        return PreparedConfigurationImport(
            document = document,
            summary = ConfigurationImportSummary(
                wifiProfilesNew = 0,
                wifiProfilesOverwritten = 0,
                devicesNew = 0,
                devicesOverwritten = 0,
                passwordsIncluded = 0,
                passwordsDeleted = 0,
                localWifiProfilesDeleted = 0,
                localDevicesDeleted = 0
            )
        )
    }
}
