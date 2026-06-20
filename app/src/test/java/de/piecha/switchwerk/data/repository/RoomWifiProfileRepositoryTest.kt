package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.local.dao.WifiProfileDao
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.domain.model.WifiSecurityType
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomWifiProfileRepositoryTest {

    @Test
    fun successfulConnectionStoresSecurityTypeAsLocallyVerified() = runBlocking {
        val dao = mock(WifiProfileDao::class.java)
        val entity = WifiProfileEntity(
            id = "wifi-1",
            name = "Device WiFi",
            ssid = "Device WiFi",
            securityType = "WPA3_SAE",
            securityTypeVerifiedLocally = false
        )
        `when`(dao.getById("wifi-1")).thenReturn(entity)
        val repository = RoomWifiProfileRepository(
            wifiProfileDao = dao,
            credentialStore = mock(WifiCredentialStore::class.java)
        )

        repository.updateLastSuccessfulSecurityType(
            id = "wifi-1",
            securityType = WifiSecurityType.WPA2
        )

        verify(dao).upsert(
            entity.copy(
                securityType = "WPA2_PSK",
                securityTypeVerifiedLocally = true
            )
        )
    }
}
