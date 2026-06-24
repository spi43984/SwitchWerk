package de.piecha.switchwerk.data.repository

import android.net.Uri
import de.piecha.switchwerk.data.transfer.ConfigurationDocument

enum class ConfigurationImportMode {
    REPLACE,
    MERGE
}

data class ConfigurationImportSummary(
    val wifiProfilesNew: Int,
    val wifiProfilesOverwritten: Int,
    val devicesNew: Int,
    val devicesOverwritten: Int,
    val passwordsIncluded: Int,
    val passwordsDeleted: Int,
    val localWifiProfilesDeleted: Int,
    val localDevicesDeleted: Int
)

data class PreparedConfigurationImport(
    val document: ConfigurationDocument,
    val summary: ConfigurationImportSummary
) {
    val containsPasswordChanges: Boolean
        get() = document.wifiProfiles.any { it.isPasswordPresent } ||
            summary.passwordsIncluded > 0 ||
            summary.passwordsDeleted > 0
}

interface ConfigurationTransferRepository {
    suspend fun exportToUri(uri: Uri, includePasswords: Boolean)

    suspend fun prepareImportFromUri(
        uri: Uri,
        mode: ConfigurationImportMode
    ): PreparedConfigurationImport

    suspend fun prepareImportFromUrl(
        url: String,
        mode: ConfigurationImportMode
    ): PreparedConfigurationImport

    suspend fun applyImport(
        preparedImport: PreparedConfigurationImport,
        mode: ConfigurationImportMode,
        includePasswords: Boolean
    )
}
