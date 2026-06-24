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
    val summary: ConfigurationImportSummary,
    val summariesByMode: Map<ConfigurationImportMode, ConfigurationImportSummary> = emptyMap()
) {
    fun summaryFor(mode: ConfigurationImportMode): ConfigurationImportSummary {
        return summariesByMode[mode] ?: summary
    }
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
