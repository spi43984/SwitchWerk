package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.transfer.CONFIGURATION_SCHEMA_VERSION
import de.piecha.switchwerk.data.transfer.ConfigurationDocument
import de.piecha.switchwerk.data.transfer.ConfigurationWifiProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigurationImportNormalizationTest {

    @Test
    fun normalizedForImportTrimsWifiProfileNameAndSsid() {
        val document = ConfigurationDocument(
            schemaVersion = CONFIGURATION_SCHEMA_VERSION,
            wifiProfiles = listOf(
                ConfigurationWifiProfile(
                    id = "wifi-1",
                    name = " Example WiFi ",
                    ssid = " Example WiFi ",
                    securityType = "WPA2_PSK"
                )
            ),
            devices = emptyList()
        )

        val normalizedProfile = document.normalizedForImport().wifiProfiles.single()

        assertEquals("Example WiFi", normalizedProfile.name)
        assertEquals("Example WiFi", normalizedProfile.ssid)
    }
}
