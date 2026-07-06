package de.piecha.switchwerk.data.transfer

data class ConfigurationDocument(
    val schemaVersion: Int,
    val wifiProfiles: List<ConfigurationWifiProfile>,
    val devices: List<ConfigurationDevice>,
    val switchGroups: List<ConfigurationSwitchGroup> = emptyList(),
    val appSettings: ConfigurationAppSettings? = null
)

data class ConfigurationAppSettings(
    val themeMode: String,
    val showActionDetails: Boolean,
    val detailPanelHeight: String,
    val diagnosticsNewestFirst: Boolean,
    val dashboardLayoutMode: String? = null,
    val language: String? = null,
    val wifiProfileSortCriterion: String? = null,
    val wifiProfileSortDirection: String? = null,
    val externalIntentsEnabled: Boolean? = null
)

data class ConfigurationWifiProfile(
    val id: String,
    val name: String,
    val ssid: String,
    val connectionMode: String = "SWITCHWERK_MANAGED",
    val securityType: String?,
    val password: String? = null,
    val isPasswordPresent: Boolean = false
)

data class ConfigurationDevice(
    val id: String,
    val name: String,
    val actionLabel: String,
    val action: ConfigurationDeviceAction,
    val connections: List<ConfigurationDeviceConnection>,
    val shortcutEnabled: Boolean = false,
    val color: String = "NONE"
)

data class ConfigurationDeviceAction(
    val protocol: String = "HTTP",
    val method: String,
    val path: String,
    val requestBody: String? = null,
    val contentType: String = "APPLICATION_JSON"
)

data class ConfigurationDeviceConnection(
    val wifiProfileId: String,
    val host: String
)

data class ConfigurationSwitchGroup(
    val id: String,
    val name: String,
    val actionLabel: String,
    val shortcutEnabled: Boolean = false,
    val color: String = "NONE",
    val errorStrategy: String = "ABORT_ON_ERROR",
    val members: List<ConfigurationSwitchGroupMember>
)

data class ConfigurationSwitchGroupMember(
    val id: String,
    val deviceId: String,
    val pauseAfterMillis: Long
)

const val CONFIGURATION_SCHEMA_VERSION = 12
