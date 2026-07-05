package de.piecha.switchwerk.data.transfer

import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.ApiContentType
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.AppLanguage
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.DeviceProtocol
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.SwitchGroupErrorStrategy
import de.piecha.switchwerk.domain.model.WifiConnectionMode
import de.piecha.switchwerk.domain.model.WifiProfileSortCriterion
import de.piecha.switchwerk.domain.model.WifiProfileSortDirection

class ConfigurationImportValidator {

    fun validate(
        document: ConfigurationDocument,
        additionalDeviceIds: Set<String> = emptySet()
    ) {
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
            settings.language?.let { language ->
                require(AppLanguage.entries.any { it.name == language }) {
                    "Nicht unterstützte Sprache: $language"
                }
            }
            settings.wifiProfileSortCriterion?.let { criterion ->
                require(WifiProfileSortCriterion.entries.any { it.name == criterion }) {
                    "Nicht unterstütztes WLAN-Sortierkriterium: $criterion"
                }
            }
            settings.wifiProfileSortDirection?.let { direction ->
                require(WifiProfileSortDirection.entries.any { it.name == direction }) {
                    "Nicht unterstützte WLAN-Sortierrichtung: $direction"
                }
            }
            require(
                (settings.wifiProfileSortCriterion == null) ==
                    (settings.wifiProfileSortDirection == null)
            ) {
                "WLAN-Sortierkriterium und WLAN-Sortierrichtung müssen gemeinsam angegeben werden"
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
        requireUniqueIds(
            ids = document.switchGroups.map { it.id },
            type = "Schaltgruppe"
        )
        requireUniqueIds(
            ids = document.switchGroups.flatMap { group -> group.members.map { it.id } },
            type = "Schaltgruppenmitglied"
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
            require(
                DeviceProtocol.entries.any { protocol -> protocol.name == device.action.protocol }
            ) {
                "Unbekanntes Geräteprotokoll: ${device.action.protocol}"
            }
            require(
                ApiMethod.entries.any { method -> method.name == device.action.method }
            ) {
                "Unbekannte API-Methode: ${device.action.method}"
            }
            require(
                ApiContentType.entries.any { contentType -> contentType.name == device.action.contentType }
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
            }
        }

        val deviceIds = document.devices.map { it.id }.toSet() + additionalDeviceIds
        document.switchGroups.forEach { group ->
            requireNotBlank(group.id, "Schaltgruppen-ID")
            requireNotBlank(group.name, "Schaltgruppenname")
            requireNotBlank(group.actionLabel, "Schaltgruppen-Button-Beschriftung")
            require(SwitchGroupErrorStrategy.entries.any { it.name == group.errorStrategy }) {
                "Nicht unterstützte Fehlerstrategie: ${group.errorStrategy}"
            }
            requireUniqueIds(
                ids = group.members.map { it.id },
                type = "Mitglied in ${group.name}"
            )
            group.members.forEach { member ->
                requireNotBlank(member.id, "Schaltgruppenmitglied-ID")
                require(member.deviceId in deviceIds) {
                    "Schaltgruppe ${group.name} verweist auf ein unbekanntes Gerät"
                }
                require(member.pauseAfterMillis in 0L..MAX_PAUSE_MILLIS) {
                    "Nicht unterstützte Pause in Schaltgruppe ${group.name}: ${member.pauseAfterMillis}"
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
        const val MAX_PAUSE_MILLIS = 3_600_000L
    }
}

private fun String.normalizedProfileName(): String = trim().lowercase()
