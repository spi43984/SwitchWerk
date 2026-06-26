package de.piecha.switchwerk.data.transfer

import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.ApiContentType
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.DeviceProtocol
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.WifiConnectionMode
import de.piecha.switchwerk.domain.validation.ApiEnumValidationResult
import de.piecha.switchwerk.domain.validation.ApiPathValidationResult
import de.piecha.switchwerk.domain.validation.HostValidationResult
import de.piecha.switchwerk.domain.validation.TechnicalFieldValidator

class ConfigurationImportValidator {

    fun validate(document: ConfigurationDocument) {
        require(document.schemaVersion in 1..CONFIGURATION_SCHEMA_VERSION) {
            "Nicht unterstützte schemaVersion: ${document.schemaVersion}"
        }

        document.appSettings?.let { settings ->
            require(AppThemeMode.entries.any { it.name == settings.themeMode }) {
                "Nicht unterstützte App-Darstellung: ${settings.themeMode}"
            }
            require(DetailPanelHeight.entries.any { it.name == settings.detailPanelHeight }) {
                "Nicht unterstützte Detailbereich-Höhe: ${settings.detailPanelHeight}"
            }
            settings.dashboardLayoutMode?.let { dashboardLayoutMode ->
                require(DashboardLayoutMode.entries.any { it.name == dashboardLayoutMode }) {
                    "Nicht unterstützte Dashboard-Darstellung: $dashboardLayoutMode"
                }
            }
        }

        requireUniqueIds(
            ids = document.wifiProfiles.map { it.id },
            type = "WLAN-Profil"
        )
        requireUniqueIds(
            ids = document.devices.map { it.id },
            type = "Gerät"
        )
        requireUniqueNames(
            names = document.wifiProfiles.map { it.name },
            type = "WLAN-Profilname"
        )

        document.wifiProfiles.forEach { profile ->
            requireNotBlank(profile.id, "WLAN-Profil-ID")
            requireNotBlank(profile.name, "WLAN-Profilname")
            requireNotBlank(profile.ssid, "SSID")
            require(
                WifiConnectionMode.entries.any { mode -> mode.name == profile.connectionMode }
            ) {
                "Nicht unterstützter WLAN-Verbindungsmodus: ${profile.connectionMode}"
            }
            profile.securityType?.let { securityType ->
                requireNotBlank(securityType, "WLAN-Sicherheitstyp")
                require(securityType in SUPPORTED_SECURITY_TYPES) {
                    "Nicht unterstützter WLAN-Sicherheitstyp: $securityType"
                }
            }
        }

        val wifiProfileIds = document.wifiProfiles.map { it.id }.toSet()
        document.devices.forEach { device ->
            requireNotBlank(device.id, "Geräte-ID")
            requireNotBlank(device.name, "Gerätename")
            requireNotBlank(device.actionLabel, "Button-Beschriftung")
            requireNotBlank(device.action.path, "API-Aufruf")
            require(TechnicalFieldValidator.validateApiPath(device.action.path) == ApiPathValidationResult.Valid) {
                "API-Aufruf ist ungültig"
            }
            require(
                DeviceProtocol.entries.any { protocol -> protocol.name == device.action.protocol }
            ) {
                "Unbekanntes Geräteprotokoll: ${device.action.protocol}"
            }
            require(
                ApiMethod.entries.any { method -> method.name == device.action.method } &&
                    TechnicalFieldValidator.validateApiMethod(device.action.method) == ApiEnumValidationResult.Valid
            ) {
                "Unbekannte API-Methode: ${device.action.method}"
            }
            require(
                ApiContentType.entries.any { contentType -> contentType.name == device.action.contentType } &&
                    TechnicalFieldValidator.validateContentType(device.action.contentType) == ApiEnumValidationResult.Valid
            ) {
                "Nicht unterstützter Content-Type: ${device.action.contentType}"
            }

            requireUniqueIds(
                ids = device.connections.map { it.wifiProfileId },
                type = "WLAN-Zuordnung für ${device.name}"
            )
            device.connections.forEach { connection ->
                require(connection.wifiProfileId in wifiProfileIds) {
                    "Gerät ${device.name} verweist auf ein unbekanntes WLAN-Profil"
                }
                requireNotBlank(connection.host, "Hostname/IP")
                require(TechnicalFieldValidator.validateHost(connection.host) == HostValidationResult.Valid) {
                    "Hostname/IP ist ungültig"
                }
            }
        }
    }

    fun validateMerge(
        document: ConfigurationDocument,
        existingWifiProfileNamesById: Map<String, String>
    ) {
        val existingProfileIdsByName = existingWifiProfileNamesById.entries.associateBy(
            keySelector = { (_, name) -> name.normalizedProfileName() },
            valueTransform = { (id, _) -> id }
        )

        document.wifiProfiles.forEach { profile ->
            val existingProfileId = existingProfileIdsByName[profile.name.normalizedProfileName()]
            require(existingProfileId == null || existingProfileId == profile.id) {
                "Import abgebrochen: WLAN-Profilname \"${profile.name.trim()}\" existiert bereits lokal."
            }
        }
    }

    private fun requireUniqueIds(ids: List<String>, type: String) {
        require(ids.size == ids.toSet().size) {
            "Doppelte IDs für $type"
        }
    }

    private fun requireNotBlank(value: String, fieldName: String) {
        require(value.isNotBlank()) {
            "$fieldName darf nicht leer sein"
        }
    }

    private fun requireUniqueNames(names: List<String>, type: String) {
        val normalizedNames = names.map { it.normalizedProfileName() }
        require(normalizedNames.size == normalizedNames.toSet().size) {
            "Doppelte Namen für $type"
        }
    }

    private companion object {
        val SUPPORTED_SECURITY_TYPES = setOf("WPA2_PSK", "WPA3_SAE")
    }
}

private fun String.normalizedProfileName(): String = trim().lowercase()
