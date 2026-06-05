package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.WifiProfile

class FakeWifiProfileRepository : WifiProfileRepository {
    override suspend fun getWifiProfiles(): List<WifiProfile> =
        listOf(
            WifiProfile(
                id = "garage-ap",
                ssid = "Shelly-Garage"
            ),
            WifiProfile(
                id = "home-wifi",
                ssid = "Home-WLAN"
            )
        )
}
