package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.domain.model.WifiSecurityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWifiProfileRepository : WifiProfileRepository {

    private val profiles = MutableStateFlow(
        listOf(
            WifiProfile(
                id = "garage-ap",
                name = "Garage",
                ssid = "Shelly-Garage"
            ),
            WifiProfile(
                id = "home-wifi",
                name = "Zuhause",
                ssid = "Home-WLAN"
            )
        )
    )

    private val passwords = mutableMapOf(
        "garage-ap" to "geheim123",
        "home-wifi" to "geheim123"
    )

    override fun observeWifiProfiles(): Flow<List<WifiProfile>> {
        return profiles.asStateFlow()
    }

    override suspend fun getWifiProfiles(): List<WifiProfile> {
        return profiles.value
    }

    override suspend fun saveWifiProfile(
        profile: WifiProfile,
        password: String?,
        shouldUpdatePassword: Boolean
    ) {
        profiles.value = profiles.value
            .filterNot { it.id == profile.id }
            .plus(profile)
            .sortedBy { it.name }

        if (shouldUpdatePassword) {
            if (password == null) {
                passwords.remove(profile.id)
            } else {
                passwords[profile.id] = password
            }
        }
    }

    override suspend fun getPassword(id: String): String? {
        return passwords[id]
    }

    override suspend fun hasPassword(id: String): Boolean {
        return passwords.containsKey(id)
    }

    override suspend fun updateLastSuccessfulSecurityType(
        id: String,
        securityType: WifiSecurityType
    ) {
        profiles.value = profiles.value.map { profile ->
            if (profile.id == id) {
                profile.copy(lastSuccessfulSecurityType = securityType)
            } else {
                profile
            }
        }
    }

    override suspend fun deletePassword(id: String) {
        passwords.remove(id)
    }

    override suspend fun deleteWifiProfile(id: String) {
        profiles.value = profiles.value.filterNot { it.id == id }
        passwords.remove(id)
    }
}
