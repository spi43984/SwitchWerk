package de.piecha.switchwerk.data.security

import org.junit.Assert.assertEquals
import org.junit.Test

class CredentialStoreFilesTest {

    @Test
    fun sharedPreferenceXmlPathsContainOnlyCredentialAndKeysetFiles() {
        assertEquals(
            listOf(
                "wifi_credentials.xml",
                "__androidx_security_crypto_encrypted_prefs_key_keyset__.xml",
                "__androidx_security_crypto_encrypted_prefs_value_keyset__.xml"
            ),
            CredentialStoreFiles.sharedPreferenceXmlPaths
        )
    }
}
