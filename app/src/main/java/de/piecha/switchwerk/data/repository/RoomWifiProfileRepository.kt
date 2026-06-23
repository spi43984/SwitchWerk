package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.local.dao.DeviceConnectionDao
import de.piecha.switchwerk.data.local.dao.WifiProfileDao
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.domain.model.WifiSecurityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomWifiProfileRepository(
    private val wifiProfileDao: WifiProfileDao,
    private val deviceConnectionDao: DeviceConnectionDao,
    private val credentialStore: WifiCredentialStore
) : WifiProfileRepository {

    override fun observeWifiProfiles(): Flow<List<WifiProfile>> {
        return wifiProfileDao.observeAll().map { profiles ->
            profiles.map { it.toDomain() }
        }
    }

    override suspend fun getWifiProfiles(): List<WifiProfile> {
        return observeWifiProfiles().first()
    }

    override suspend fun saveWifiProfile(
        profile: WifiProfile,
        password: String?,
        shouldUpdatePassword: Boolean
    ) {
        require(profile.name.isNotBlank()) {
            "Profilname darf nicht leer sein"
        }
        require(profile.ssid.isNotBlank()) {
            "SSID darf nicht leer sein"
        }
        require(
            wifiProfileDao.getAll().none {
                it.id != profile.id && it.name.equals(profile.name, ignoreCase = true)
            }
        ) {
            "Profilname ist bereits vergeben"
        }

        val existingProfile = wifiProfileDao.getById(profile.id)
        wifiProfileDao.upsert(
            profile.toEntity(
                existingSecurityType = existingProfile?.securityType,
                existingSecurityTypeVerifiedLocally =
                    existingProfile?.securityTypeVerifiedLocally ?: true
            )
        )

        if (shouldUpdatePassword) {
            if (password == null) {
                credentialStore.deletePassword(profile.id)
            } else {
                credentialStore.savePassword(
                    wifiProfileId = profile.id,
                    password = password
                )
            }
        }
    }

    override suspend fun getPassword(id: String): String? {
        return credentialStore.getPassword(id)
    }

    override suspend fun hasPassword(id: String): Boolean {
        return getPassword(id) != null
    }

    override suspend fun updateLastSuccessfulSecurityType(
        id: String,
        securityType: WifiSecurityType
    ) {
        val entity = wifiProfileDao.getById(id) ?: return
        wifiProfileDao.upsert(
            entity.copy(
                securityType = securityType.storageValue,
                securityTypeVerifiedLocally = true
            )
        )
    }

    override suspend fun deletePassword(id: String) {
        credentialStore.deletePassword(id)
    }

    override suspend fun deleteWifiProfile(id: String) {
        deviceConnectionDao.deleteForWifiProfile(id)
        credentialStore.deletePassword(id)
        wifiProfileDao.deleteById(id)
    }

    private fun WifiProfileEntity.toDomain(): WifiProfile {
        return WifiProfile(
            id = id,
            ssid = ssid,
            name = name,
            lastSuccessfulSecurityType = WifiSecurityType.fromStorageValue(securityType),
            isSecurityTypeVerifiedLocally = securityTypeVerifiedLocally
        )
    }

    private fun WifiProfile.toEntity(
        existingSecurityType: String?,
        existingSecurityTypeVerifiedLocally: Boolean
    ): WifiProfileEntity {
        return WifiProfileEntity(
            id = id,
            name = name,
            ssid = ssid,
            securityType = lastSuccessfulSecurityType?.storageValue ?: existingSecurityType,
            securityTypeVerifiedLocally = if (lastSuccessfulSecurityType != null) {
                isSecurityTypeVerifiedLocally
            } else {
                existingSecurityTypeVerifiedLocally
            }
        )
    }
}
