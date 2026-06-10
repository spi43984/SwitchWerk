package de.piecha.switchwerk.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedWifiCredentialStore(
    context: Context
) : WifiCredentialStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun savePassword(wifiProfileId: String, password: String) {
        sharedPreferences.edit()
            .putString(passwordKey(wifiProfileId), password)
            .apply()
    }

    override suspend fun getPassword(wifiProfileId: String): String? {
        return sharedPreferences.getString(passwordKey(wifiProfileId), null)
    }

    override suspend fun deletePassword(wifiProfileId: String) {
        sharedPreferences.edit()
            .remove(passwordKey(wifiProfileId))
            .apply()
    }

    private fun passwordKey(wifiProfileId: String): String {
        return "$PASSWORD_KEY_PREFIX$wifiProfileId"
    }

    private companion object {
        const val FILE_NAME = "wifi_credentials"
        const val PASSWORD_KEY_PREFIX = "wifi_password_"
    }
}
