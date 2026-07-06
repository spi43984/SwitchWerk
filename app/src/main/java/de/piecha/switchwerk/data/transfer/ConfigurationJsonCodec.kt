package de.piecha.switchwerk.data.transfer

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import java.io.StringReader
import java.io.StringWriter
import java.util.UUID

class ConfigurationJsonCodec {

    fun encode(document: ConfigurationDocument): String {
        return StringWriter().use { output ->
            JsonWriter(output).use { writer ->
                writer.setIndent("  ")
                writer.beginObject()
                writer.name("schemaVersion").value(document.schemaVersion.toLong())
                document.appSettings?.let { settings ->
                    writer.name("appSettings")
                    writer.beginObject()
                    writer.name("themeMode").value(settings.themeMode)
                    writer.name("showActionDetails").value(settings.showActionDetails)
                    writer.name("detailPanelHeight").value(settings.detailPanelHeight)
                    writer.name("diagnosticsNewestFirst").value(settings.diagnosticsNewestFirst)
                    settings.dashboardLayoutMode?.let { dashboardLayoutMode ->
                        writer.name("dashboardLayoutMode").value(dashboardLayoutMode)
                    }
                    settings.language?.let { writer.name("language").value(it) }
                    settings.wifiProfileSortCriterion?.let {
                        writer.name("wifiProfileSortCriterion").value(it)
                    }
                    settings.wifiProfileSortDirection?.let {
                        writer.name("wifiProfileSortDirection").value(it)
                    }
                    settings.externalIntentsEnabled?.let {
                        writer.name("externalIntentsEnabled").value(it)
                    }
                    writer.endObject()
                }
                writer.name("wifiProfiles")
                writer.beginArray()
                document.wifiProfiles.forEach { profile ->
                    writer.beginObject()
                    writer.name("id").value(profile.id)
                    writer.name("name").value(profile.name)
                    writer.name("ssid").value(profile.ssid)
                    writer.name("connectionMode").value(profile.connectionMode)
                    profile.securityType?.let { securityType ->
                        writer.name("securityType").value(securityType)
                    }
                    if (profile.isPasswordPresent) {
                        writer.name("password").value(profile.password.orEmpty())
                    }
                    writer.endObject()
                }
                writer.endArray()
                writer.name("devices")
                writer.beginArray()
                document.devices.forEach { device ->
                    writer.beginObject()
                    writer.name("id").value(device.id)
                    writer.name("name").value(device.name)
                    writer.name("actionLabel").value(device.actionLabel)
                    writer.name("shortcutEnabled").value(device.shortcutEnabled)
                    writer.name("color").value(device.color)
                    writer.name("action")
                    writer.beginObject()
                    writer.name("protocol").value(device.action.protocol)
                    writer.name("method").value(device.action.method)
                    writer.name("path").value(device.action.path)
                    device.action.requestBody?.let { requestBody ->
                        writer.name("requestBody").value(requestBody)
                    }
                    writer.name("contentType").value(device.action.contentType)
                    writer.endObject()
                    writer.name("connections")
                    writer.beginArray()
                    device.connections.forEach { connection ->
                        writer.beginObject()
                        writer.name("wifiProfileId").value(connection.wifiProfileId)
                        writer.name("host").value(connection.host)
                        writer.endObject()
                    }
                    writer.endArray()
                    writer.endObject()
                }
                writer.endArray()
                writer.name("switchGroups")
                writer.beginArray()
                document.switchGroups.forEach { group ->
                    writer.beginObject()
                    writer.name("id").value(group.id)
                    writer.name("name").value(group.name)
                    writer.name("actionLabel").value(group.actionLabel)
                    writer.name("shortcutEnabled").value(group.shortcutEnabled)
                    writer.name("color").value(group.color)
                    writer.name("errorStrategy").value(group.errorStrategy)
                    writer.name("members")
                    writer.beginArray()
                    group.members.forEach { member ->
                        writer.beginObject()
                        writer.name("id").value(member.id)
                        writer.name("deviceId").value(member.deviceId)
                        writer.name("pauseAfterMillis").value(member.pauseAfterMillis)
                        writer.endObject()
                    }
                    writer.endArray()
                    writer.endObject()
                }
                writer.endArray()
                writer.endObject()
            }
            output.toString()
        }
    }

    fun decode(json: String): ConfigurationDocument {
        return JsonReader(StringReader(json)).use { reader ->
            var schemaVersion: Int? = null
            var wifiProfiles: List<ConfigurationWifiProfile>? = null
            var devices: List<ConfigurationDevice>? = null
            var switchGroups: List<ConfigurationSwitchGroup> = emptyList()
            var appSettings: ConfigurationAppSettings? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "schemaVersion" -> schemaVersion = reader.nextInt()
                    "appSettings" -> appSettings = reader.readAppSettings()
                    "wifiProfiles" -> wifiProfiles = reader.readWifiProfiles()
                    "devices" -> devices = reader.readDevices()
                    "switchGroups" -> switchGroups = reader.readSwitchGroups()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            ConfigurationDocument(
                schemaVersion = requireField(schemaVersion, "schemaVersion"),
                wifiProfiles = requireField(wifiProfiles, "wifiProfiles"),
                devices = requireField(devices, "devices"),
                switchGroups = switchGroups,
                appSettings = appSettings
            )
        }
    }

    private fun JsonReader.readAppSettings(): ConfigurationAppSettings {
        var themeMode: String? = null
        var showActionDetails: Boolean? = null
        var detailPanelHeight: String? = null
        var diagnosticsNewestFirst: Boolean? = null
        var dashboardLayoutMode: String? = null
        var language: String? = null
        var wifiProfileSortCriterion: String? = null
        var wifiProfileSortDirection: String? = null
        var externalIntentsEnabled: Boolean? = null

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "themeMode" -> themeMode = nextString()
                "showActionDetails" -> showActionDetails = nextBoolean()
                "detailPanelHeight" -> detailPanelHeight = nextString()
                "diagnosticsNewestFirst" -> diagnosticsNewestFirst = nextBoolean()
                "dashboardLayoutMode" -> dashboardLayoutMode = nextString()
                "language" -> language = nextString()
                "wifiProfileSortCriterion" -> wifiProfileSortCriterion = nextString()
                "wifiProfileSortDirection" -> wifiProfileSortDirection = nextString()
                "externalIntentsEnabled" -> externalIntentsEnabled = nextBoolean()
                else -> skipValue()
            }
        }
        endObject()

        return ConfigurationAppSettings(
            themeMode = requireField(themeMode, "appSettings.themeMode"),
            showActionDetails = requireField(
                showActionDetails,
                "appSettings.showActionDetails"
            ),
            detailPanelHeight = requireField(
                detailPanelHeight,
                "appSettings.detailPanelHeight"
            ),
            diagnosticsNewestFirst = requireField(
                diagnosticsNewestFirst,
                "appSettings.diagnosticsNewestFirst"
            ),
            dashboardLayoutMode = dashboardLayoutMode,
            language = language,
            wifiProfileSortCriterion = wifiProfileSortCriterion,
            wifiProfileSortDirection = wifiProfileSortDirection,
            externalIntentsEnabled = externalIntentsEnabled
        )
    }

    private fun JsonReader.readWifiProfiles(): List<ConfigurationWifiProfile> {
        val profiles = mutableListOf<ConfigurationWifiProfile>()
        beginArray()
        while (hasNext()) {
            var id: String? = null
            var name: String? = null
            var ssid: String? = null
            var connectionMode: String? = null
            var securityType: String? = null
            var password: String? = null
            var isPasswordPresent = false

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "id" -> id = nextString()
                    "name" -> name = nextString()
                    "ssid" -> ssid = nextString()
                    "connectionMode" -> connectionMode = nextString()
                    "securityType" -> {
                        securityType = if (peek() == JsonToken.NULL) {
                            nextNull()
                            null
                        } else {
                            nextString()
                        }
                    }
                    "password" -> {
                        isPasswordPresent = true
                        if (peek() == JsonToken.NULL) {
                            nextNull()
                            throw IllegalArgumentException("Passwort darf nicht null sein")
                        }
                        password = nextString()
                    }
                    else -> skipValue()
                }
            }
            endObject()

            val requiredSsid = requireField(ssid, "wifiProfiles.ssid")
            profiles += ConfigurationWifiProfile(
                id = requireField(id, "wifiProfiles.id"),
                name = name ?: requiredSsid,
                ssid = requiredSsid,
                connectionMode = connectionMode ?: "SWITCHWERK_MANAGED",
                securityType = securityType,
                password = password,
                isPasswordPresent = isPasswordPresent
            )
        }
        endArray()
        return profiles
    }

    private fun JsonReader.readDevices(): List<ConfigurationDevice> {
        val devices = mutableListOf<ConfigurationDevice>()
        beginArray()
        while (hasNext()) {
            var id: String? = null
            var name: String? = null
            var actionLabel: String? = null
            var action: ConfigurationDeviceAction? = null
            var connections: List<ConfigurationDeviceConnection>? = null
            var shortcutEnabled = false
            var color = "NONE"

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "id" -> id = nextString()
                    "name" -> name = nextString()
                    "actionLabel" -> actionLabel = nextString()
                    "shortcutEnabled" -> shortcutEnabled = nextBoolean()
                    "color" -> color = nextString()
                    "action" -> action = readAction()
                    "connections" -> connections = readConnections()
                    else -> skipValue()
                }
            }
            endObject()

            devices += ConfigurationDevice(
                id = requireField(id, "devices.id"),
                name = requireField(name, "devices.name"),
                actionLabel = requireField(actionLabel, "devices.actionLabel"),
                action = requireField(action, "devices.action"),
                connections = requireField(connections, "devices.connections"),
                shortcutEnabled = shortcutEnabled,
                color = color
            )
        }
        endArray()
        return devices
    }

    private fun JsonReader.readAction(): ConfigurationDeviceAction {
        var protocol: String? = null
        var method: String? = null
        var path: String? = null
        var requestBody: String? = null
        var contentType: String? = null

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "protocol" -> protocol = nextString()
                "method" -> method = nextString()
                "path" -> path = nextString()
                "requestBody" -> {
                    requestBody = if (peek() == JsonToken.NULL) {
                        nextNull()
                        null
                    } else {
                        nextString()
                    }
                }
                "contentType" -> contentType = nextString()
                else -> skipValue()
            }
        }
        endObject()

        return ConfigurationDeviceAction(
            protocol = protocol ?: "HTTP",
            method = requireField(method, "devices.action.method"),
            path = requireField(path, "devices.action.path"),
            requestBody = requestBody,
            contentType = contentType ?: "APPLICATION_JSON"
        )
    }

    private fun JsonReader.readConnections(): List<ConfigurationDeviceConnection> {
        val connections = mutableListOf<ConfigurationDeviceConnection>()
        beginArray()
        while (hasNext()) {
            var wifiProfileId: String? = null
            var host: String? = null

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "wifiProfileId" -> wifiProfileId = nextString()
                    "host" -> host = nextString()
                    else -> skipValue()
                }
            }
            endObject()

            connections += ConfigurationDeviceConnection(
                wifiProfileId = requireField(wifiProfileId, "connections.wifiProfileId"),
                host = requireField(host, "connections.host")
            )
        }
        endArray()
        return connections
    }

    private fun JsonReader.readSwitchGroups(): List<ConfigurationSwitchGroup> {
        val groups = mutableListOf<ConfigurationSwitchGroup>()
        beginArray()
        while (hasNext()) {
            var id: String? = null
            var name: String? = null
            var actionLabel: String? = null
            var shortcutEnabled = false
            var color = "NONE"
            var errorStrategy: String? = null
            var members: List<ConfigurationSwitchGroupMember>? = null

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "id" -> id = nextString()
                    "name" -> name = nextString()
                    "actionLabel" -> actionLabel = nextString()
                    "shortcutEnabled" -> shortcutEnabled = nextBoolean()
                    "color" -> color = nextString()
                    "errorStrategy" -> errorStrategy = nextString()
                    "members" -> members = readSwitchGroupMembers()
                    else -> skipValue()
                }
            }
            endObject()

            groups += ConfigurationSwitchGroup(
                id = requireField(id, "switchGroups.id"),
                name = requireField(name, "switchGroups.name"),
                actionLabel = requireField(actionLabel, "switchGroups.actionLabel"),
                shortcutEnabled = shortcutEnabled,
                color = color,
                errorStrategy = errorStrategy ?: "ABORT_ON_ERROR",
                members = requireField(members, "switchGroups.members")
            )
        }
        endArray()
        return groups
    }

    private fun JsonReader.readSwitchGroupMembers(): List<ConfigurationSwitchGroupMember> {
        val members = mutableListOf<ConfigurationSwitchGroupMember>()
        beginArray()
        while (hasNext()) {
            var id: String? = null
            var deviceId: String? = null
            var pauseAfterMillis: Long? = null

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "id" -> id = nextString()
                    "deviceId" -> deviceId = nextString()
                    "pauseAfterMillis" -> pauseAfterMillis = nextLong()
                    else -> skipValue()
                }
            }
            endObject()

            members += ConfigurationSwitchGroupMember(
                id = id ?: UUID.randomUUID().toString(),
                deviceId = requireField(deviceId, "switchGroups.members.deviceId"),
                pauseAfterMillis = pauseAfterMillis ?: 0L
            )
        }
        endArray()
        return members
    }

    private fun <T> requireField(value: T?, fieldName: String): T {
        return value ?: throw IllegalArgumentException("Pflichtfeld fehlt: $fieldName")
    }
}
