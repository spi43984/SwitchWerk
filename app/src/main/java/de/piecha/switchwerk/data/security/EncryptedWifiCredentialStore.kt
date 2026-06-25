package de.piecha.switchwerk.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

class EncryptedWifiCredentialStore(
    context: Context
) : WifiCredentialStore {

    private val appContext = context.applicationContext

    @Volatile
    private var sharedPreferences = createSharedPreferencesWithRecovery()

    override suspend fun savePassword(wifiProfileId: String, password: String) {
        withRecoveryAfterReadOrWriteFailure {
            edit()
                .putString(passwordKey(wifiProfileId), password)
                .apply()
        }
    }

    override suspend fun getPassword(wifiProfileId: String): String? {
        return withRecoveryAfterReadOrWriteFailure {
            getString(passwordKey(wifiProfileId), null)
        }
    }

    override suspend fun deletePassword(wifiProfileId: String) {
        withRecoveryAfterReadOrWriteFailure {
            edit()
                .remove(passwordKey(wifiProfileId))
                .apply()
        }
    }

    private fun createSharedPreferencesWithRecovery(): SharedPreferences {
        return try {
            createEncryptedSharedPreferences()
        } catch (error: GeneralSecurityException) {
            recreateAfterDeletingCredentialFiles()
        } catch (error: IOException) {
            recreateAfterDeletingCredentialFiles()
        } catch (error: RuntimeException) {
            if (error.isRecoverableEncryptedStorageFailure()) {
                recreateAfterDeletingCredentialFiles()
            } else {
                throw error
            }
        }
    }

    private fun recreateAfterDeletingCredentialFiles(): SharedPreferences {
        CredentialStoreFiles.delete(appContext)
        return try {
            createEncryptedSharedPreferences()
        } catch (retryError: GeneralSecurityException) {
            throw CredentialStoreInitializationException(retryError)
        } catch (retryError: IOException) {
            throw CredentialStoreInitializationException(retryError)
        } catch (retryError: RuntimeException) {
            throw CredentialStoreInitializationException(retryError)
        }
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            appContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun <T> withRecoveryAfterReadOrWriteFailure(
        operation: SharedPreferences.() -> T
    ): T {
        return try {
            sharedPreferences.operation()
        } catch (error: RuntimeException) {
            if (error.isRecoverableEncryptedStorageFailure()) {
                synchronized(this) {
                    CredentialStoreFiles.delete(appContext)
                    sharedPreferences = recreateAfterDeletingCredentialFiles()
                    sharedPreferences.operation()
                }
            } else {
                throw error
            }
        }
    }

    private fun passwordKey(wifiProfileId: String): String {
        return "$PASSWORD_KEY_PREFIX$wifiProfileId"
    }

    private companion object {
        const val FILE_NAME = "wifi_credentials"
        const val PASSWORD_KEY_PREFIX = "wifi_password_"
    }
}

private fun Throwable.isRecoverableEncryptedStorageFailure(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is GeneralSecurityException || current is IOException || current is SecurityException) {
            return true
        }
        current = current.cause
    }
    return false
}

internal class CredentialStoreInitializationException(
    cause: Throwable
) : IllegalStateException(
    "Encrypted credential storage could not be initialized after recovery.",
    cause
)

internal object CredentialStoreFiles {
    val sharedPreferenceNames = listOf(
        "wifi_credentials",
        "__androidx_security_crypto_encrypted_prefs_key_keyset__",
        "__androidx_security_crypto_encrypted_prefs_value_keyset__"
    )

    val sharedPreferenceXmlPaths = sharedPreferenceNames.map { "$it.xml" }

    fun delete(context: Context) {
        sharedPreferenceNames.forEach(context::deleteSharedPreferences)
    }
}
