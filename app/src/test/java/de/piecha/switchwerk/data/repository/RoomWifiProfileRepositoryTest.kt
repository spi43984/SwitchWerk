package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.local.dao.DeviceConnectionDao
import de.piecha.switchwerk.data.local.dao.WifiProfileDao
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.domain.model.WifiSecurityType
import de.piecha.switchwerk.domain.model.WifiConnectionMode
import de.piecha.switchwerk.domain.model.WifiProfile
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
            connectionMode = WifiConnectionMode.SWITCHWERK_MANAGED.name,
            securityType = "WPA3_SAE",
            securityTypeVerifiedLocally = false
        )
        `when`(dao.getById("wifi-1")).thenReturn(entity)
        val repository = RoomWifiProfileRepository(
            wifiProfileDao = dao,
            deviceConnectionDao = mock(DeviceConnectionDao::class.java),
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

    @Test
    fun deletingWifiProfileRemovesDeviceConnections() = runBlocking {
        val wifiProfileDao = mock(WifiProfileDao::class.java)
        val deviceConnectionDao = mock(DeviceConnectionDao::class.java)
        val credentialStore = mock(WifiCredentialStore::class.java)
        val repository = RoomWifiProfileRepository(
            wifiProfileDao = wifiProfileDao,
            deviceConnectionDao = deviceConnectionDao,
            credentialStore = credentialStore
        )

        repository.deleteWifiProfile("wifi-1")

        verify(deviceConnectionDao).deleteForWifiProfile("wifi-1")
        verify(wifiProfileDao).deleteById("wifi-1")
        verify(credentialStore).deletePassword("wifi-1")
    }

    @Test
    fun androidManagedProfileDeletesExistingPasswordWhenSaved() = runBlocking {
        val wifiProfileDao = mock(WifiProfileDao::class.java)
        val credentialStore = mock(WifiCredentialStore::class.java)
        `when`(wifiProfileDao.getAll()).thenReturn(emptyList())
        `when`(wifiProfileDao.getById("wifi-1")).thenReturn(null)
        val repository = RoomWifiProfileRepository(
            wifiProfileDao = wifiProfileDao,
            deviceConnectionDao = mock(DeviceConnectionDao::class.java),
            credentialStore = credentialStore
        )

        repository.saveWifiProfile(
            profile = WifiProfile(
                id = "wifi-1",
                name = "Office",
                ssid = "Office",
                connectionMode = WifiConnectionMode.ANDROID_MANAGED
            ),
            password = "secret",
            shouldUpdatePassword = true
        )

        verify(credentialStore).deletePassword("wifi-1")
    }
}
