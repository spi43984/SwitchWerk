package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.WifiProfile
import kotlinx.coroutines.flow.Flow

interface WifiProfileRepository {
    fun observeWifiProfiles(): Flow<List<WifiProfile>>

    suspend fun getWifiProfiles(): List<WifiProfile>

    suspend fun saveWifiProfile(
        profile: WifiProfile,
        password: String?,
        shouldUpdatePassword: Boolean
    )

    suspend fun getPassword(id: String): String?

    suspend fun hasPassword(id: String): Boolean

    suspend fun deletePassword(id: String)

    suspend fun deleteWifiProfile(id: String)
}
