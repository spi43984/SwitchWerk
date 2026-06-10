package de.piecha.switchwerk.data.security

interface WifiCredentialStore {
    suspend fun savePassword(wifiProfileId: String, password: String)

    suspend fun getPassword(wifiProfileId: String): String?

    suspend fun deletePassword(wifiProfileId: String)
}
