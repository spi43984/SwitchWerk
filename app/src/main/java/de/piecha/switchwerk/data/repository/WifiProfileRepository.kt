package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.WifiProfile

interface WifiProfileRepository {
    suspend fun getWifiProfiles(): List<WifiProfile>
}
