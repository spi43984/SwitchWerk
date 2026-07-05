package de.piecha.switchwerk.data.repository

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import de.piecha.switchwerk.data.local.AppDatabase
import de.piecha.switchwerk.data.local.dao.DeviceConnectionDao
import de.piecha.switchwerk.data.local.dao.DeviceDao
import de.piecha.switchwerk.data.local.dao.SwitchGroupDao
import de.piecha.switchwerk.data.local.dao.SwitchGroupMemberDao
import de.piecha.switchwerk.data.local.dao.WifiProfileDao
import de.piecha.switchwerk.data.local.entity.DeviceConnectionEntity
import de.piecha.switchwerk.data.local.entity.DeviceEntity
import de.piecha.switchwerk.data.local.entity.SwitchGroupEntity
import de.piecha.switchwerk.data.local.entity.SwitchGroupMemberEntity
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.data.transfer.CONFIGURATION_SCHEMA_VERSION
import de.piecha.switchwerk.data.transfer.ConfigurationAppSettings
import de.piecha.switchwerk.data.transfer.ConfigurationDevice
import de.piecha.switchwerk.data.transfer.ConfigurationDeviceAction
import de.piecha.switchwerk.data.transfer.ConfigurationDeviceConnection
import de.piecha.switchwerk.data.transfer.ConfigurationDocument
import de.piecha.switchwerk.data.transfer.ConfigurationImportValidator
import de.piecha.switchwerk.data.transfer.ConfigurationJsonCodec
import de.piecha.switchwerk.data.transfer.ConfigurationSwitchGroup
import de.piecha.switchwerk.data.transfer.ConfigurationSwitchGroupMember
import de.piecha.switchwerk.data.transfer.ConfigurationWifiProfile
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.AppLanguage
import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.WifiProfileSortCriterion
import de.piecha.switchwerk.domain.model.WifiProfileSortDirection
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request

class DefaultConfigurationTransferRepository(
    private val contentResolver: ContentResolver,
    private val database: AppDatabase,
    private val deviceDao: DeviceDao,
    private val deviceConnectionDao: DeviceConnectionDao,
    private val switchGroupDao: SwitchGroupDao,
    private val switchGroupMemberDao: SwitchGroupMemberDao,
    private val wifiProfileDao: WifiProfileDao,
    private val credentialStore: WifiCredentialStore,
    private val httpClient: OkHttpClient,
    private val jsonCodec: ConfigurationJsonCodec,
    private val validator: ConfigurationImportValidator,
    private val appSettingsRepository: AppSettingsRepository
) : ConfigurationTransferRepository {

    override suspend fun exportToUri(uri: Uri, includePasswords: Boolean) {
        withContext(Dispatchers.IO) {
            val document = buildExportDocument(includePasswords)
            val json = jsonCodec.encode(document)
            val output = contentResolver.openOutputStream(uri, "wt")
                ?: throw IllegalArgumentException("Exportdatei konnte nicht geöffnet werden")
            output.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                writer.write(json)
            }
        }
    }

    override suspend fun prepareImportFromUri(
        uri: Uri,
        mode: ConfigurationImportMode
    ): PreparedConfigurationImport {
        return withContext(Dispatchers.IO) {
            val input = contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Importdatei konnte nicht geöffnet werden")
            prepareImport(
                json = input.use(::readLimitedUtf8),
                mode = mode
            )
        }
    }

    override suspend fun prepareImportFromUrl(
        url: String,
        mode: ConfigurationImportMode
    ): PreparedConfigurationImport {
        return withContext(Dispatchers.IO) {
            val requestUrl = importHttpUrl(url)
            val json = fetchImportJson(
                googleDriveDownloadUrl(requestUrl) ?: requestUrl
            )
            prepareImport(
                json = json,
                mode = mode
            )
        }
    }

    override suspend fun applyImport(
        preparedImport: PreparedConfigurationImport,
        mode: ConfigurationImportMode,
        includePasswords: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val document = preparedImport.document.normalizedForImport()
            val existingDeviceIds = deviceDao.getAll().map { it.id }.toSet()
            validator.validate(
                document = document,
                additionalDeviceIds = if (mode == ConfigurationImportMode.MERGE) {
                    existingDeviceIds
                } else {
                    emptySet()
                }
            )
            if (mode == ConfigurationImportMode.MERGE) {
                validator.validateMerge(
                    document = document,
                    existingWifiProfileNamesById = wifiProfileDao.getAll().associate { it.id to it.name }
                )
            }
            val oldWifiProfileIds = wifiProfileDao.getAll().map { it.id }

            database.withTransaction {
                when (mode) {
                    ConfigurationImportMode.REPLACE -> replaceConfiguration(document)
                    ConfigurationImportMode.MERGE -> mergeConfiguration(document)
                }
            }

            if (mode == ConfigurationImportMode.REPLACE) {
                oldWifiProfileIds.forEach { credentialStore.deletePassword(it) }
            }
            if (includePasswords) {
                applyImportedPasswords(document)
            }
            applyImportedAppSettings(document)
        }
    }

    private suspend fun buildExportDocument(includePasswords: Boolean): ConfigurationDocument {
        val profiles = wifiProfileDao.getAll()
        val devices = deviceDao.getAll()
        val connections = deviceConnectionDao.getAll()
        val groups = switchGroupDao.getAll()
        val groupMembers = switchGroupMemberDao.getAll()

        return ConfigurationDocument(
            schemaVersion = CONFIGURATION_SCHEMA_VERSION,
            appSettings = appSettingsRepository.settings.value.toConfigurationAppSettings(),
            wifiProfiles = profiles.map { profile ->
                ConfigurationWifiProfile(
                    id = profile.id,
                    name = profile.name,
                    ssid = profile.ssid,
                    connectionMode = profile.connectionMode,
                    securityType = profile.securityType,
                    password = if (includePasswords) {
                        credentialStore.getPassword(profile.id).orEmpty()
                    } else {
                        null
                    },
                    isPasswordPresent = includePasswords
                )
            },
            devices = devices.map { device ->
                ConfigurationDevice(
                    id = device.id,
                    name = device.name,
                    actionLabel = device.actionLabel,
                    shortcutEnabled = device.shortcutEnabled,
                    action = ConfigurationDeviceAction(
                        protocol = device.apiProtocol,
                        method = device.apiMethod,
                        path = device.apiPath,
                        requestBody = device.apiRequestBody.ifEmpty { null },
                        contentType = device.apiContentType
                    ),
                    connections = connections
                        .filter { it.deviceId == device.id }
                        .sortedBy { it.priority }
                        .map { connection ->
                            ConfigurationDeviceConnection(
                                wifiProfileId = connection.wifiProfileId,
                                host = connection.host
                            )
                        }
                )
            },
            switchGroups = groups.map { group ->
                ConfigurationSwitchGroup(
                    id = group.id,
                    name = group.name,
                    actionLabel = group.actionLabel,
                    errorStrategy = group.errorStrategy,
                    members = groupMembers
                        .filter { it.groupId == group.id }
                        .sortedBy { it.sortOrder }
                        .map { member ->
                            ConfigurationSwitchGroupMember(
                                id = member.id,
                                deviceId = member.deviceId,
                                pauseAfterMillis = member.pauseAfterMillis
                            )
                        }
                )
            }
        )
    }

    private suspend fun prepareImport(
        json: String,
        mode: ConfigurationImportMode
    ): PreparedConfigurationImport {
        require(json.trimStart().startsWith("{")) {
            "Die URL liefert keine gültige SwitchWerk-JSON-Datei"
        }
        val document = runCatching { jsonCodec.decode(json) }
            .getOrElse { error ->
                throw IllegalArgumentException(
                    "Importdatei enthält kein gültiges JSON",
                    error
                )
            }.normalizedForImport()
        val existingWifiProfiles = wifiProfileDao.getAll()
        val existingDeviceIds = deviceDao.getAll().map { it.id }.toSet()
        validator.validate(
            document = document,
            additionalDeviceIds = if (mode == ConfigurationImportMode.MERGE) {
                existingDeviceIds
            } else {
                emptySet()
            }
        )
        if (mode == ConfigurationImportMode.MERGE) {
            validator.validateMerge(
                document = document,
                existingWifiProfileNamesById = existingWifiProfiles.associate { it.id to it.name }
            )
        }
        val existingWifiProfileIds = existingWifiProfiles.map { it.id }.toSet()

        val summariesByMode = ConfigurationImportMode.entries.associateWith { importMode ->
            createImportSummary(
                document = document,
                mode = importMode,
                existingWifiProfileIds = existingWifiProfileIds,
                existingDeviceIds = existingDeviceIds
            )
        }

        return PreparedConfigurationImport(
            document = document,
            summary = requireNotNull(summariesByMode[mode]),
            summariesByMode = summariesByMode
        )
    }

    private fun createImportSummary(
        document: ConfigurationDocument,
        mode: ConfigurationImportMode,
        existingWifiProfileIds: Set<String>,
        existingDeviceIds: Set<String>
    ): ConfigurationImportSummary {
        val importedWifiProfileIds = document.wifiProfiles.map { it.id }.toSet()
        val importedDeviceIds = document.devices.map { it.id }.toSet()
        val externalIntentsEnabledChange = document.appSettings.externalIntentsChangeFrom(
            appSettingsRepository.settings.value
        )

        return when (mode) {
            ConfigurationImportMode.MERGE -> ConfigurationImportSummary(
                wifiProfilesNew = importedWifiProfileIds.count { it !in existingWifiProfileIds },
                wifiProfilesOverwritten = importedWifiProfileIds.count { it in existingWifiProfileIds },
                devicesNew = importedDeviceIds.count { it !in existingDeviceIds },
                devicesOverwritten = importedDeviceIds.count { it in existingDeviceIds },
                passwordsIncluded = document.passwordCount(),
                passwordsDeleted = document.passwordDeletionCount(),
                localWifiProfilesDeleted = 0,
                localDevicesDeleted = 0,
                externalIntentsEnabledChange = externalIntentsEnabledChange
            )

            ConfigurationImportMode.REPLACE -> ConfigurationImportSummary(
                wifiProfilesNew = document.wifiProfiles.size,
                wifiProfilesOverwritten = 0,
                devicesNew = document.devices.size,
                devicesOverwritten = 0,
                passwordsIncluded = document.passwordCount(),
                passwordsDeleted = document.passwordDeletionCount(),
                localWifiProfilesDeleted = existingWifiProfileIds.size,
                localDevicesDeleted = existingDeviceIds.size,
                externalIntentsEnabledChange = externalIntentsEnabledChange
            )
        }
    }

    private suspend fun replaceConfiguration(document: ConfigurationDocument) {
        switchGroupMemberDao.deleteAll()
        switchGroupDao.deleteAll()
        deviceConnectionDao.deleteAll()
        deviceDao.deleteAll()
        wifiProfileDao.deleteAll()
        wifiProfileDao.upsertAll(document.wifiProfiles.map { it.toEntity() })
        deviceDao.upsertAll(
            document.devices.mapIndexed { index, device ->
                device.toEntity(sortOrder = index)
            }
        )
        deviceConnectionDao.upsertAll(document.toConnectionEntities())
        switchGroupDao.upsertAll(
            document.switchGroups.mapIndexed { index, group ->
                group.toEntity(sortOrder = document.devices.size + index)
            }
        )
        switchGroupMemberDao.upsertAll(document.toSwitchGroupMemberEntities())
    }

    private suspend fun mergeConfiguration(document: ConfigurationDocument) {
        val existingDevices = deviceDao.getAll()
        val existingSortOrders = existingDevices.associate { it.id to it.sortOrder }
        val existingGroups = switchGroupDao.getAll()
        val existingGroupSortOrders = existingGroups.associate { it.id to it.sortOrder }
        var nextSortOrder = (
            (existingDevices.map { it.sortOrder } + existingGroups.map { it.sortOrder })
                .maxOrNull() ?: -1
        ) + 1

        wifiProfileDao.upsertAll(document.wifiProfiles.map { it.toEntity() })
        deviceDao.upsertAll(
            document.devices.map { device ->
                val sortOrder = existingSortOrders[device.id] ?: nextSortOrder++
                device.toEntity(sortOrder)
            }
        )
        document.devices.forEach { device ->
            deviceConnectionDao.deleteForDevice(device.id)
        }
        deviceConnectionDao.upsertAll(document.toConnectionEntities())
        switchGroupDao.upsertAll(
            document.switchGroups.map { group ->
                val sortOrder = existingGroupSortOrders[group.id] ?: nextSortOrder++
                group.toEntity(sortOrder)
            }
        )
        document.switchGroups.forEach { group ->
            switchGroupMemberDao.deleteForGroup(group.id)
        }
        switchGroupMemberDao.upsertAll(document.toSwitchGroupMemberEntities())
    }

    private suspend fun applyImportedPasswords(document: ConfigurationDocument) {
        document.wifiProfiles
            .filter { it.isPasswordPresent }
            .forEach { profile ->
                val password = profile.password.orEmpty()
                if (password.isEmpty()) {
                    credentialStore.deletePassword(profile.id)
                } else {
                    credentialStore.savePassword(profile.id, password)
                }
            }
    }

    private fun applyImportedAppSettings(document: ConfigurationDocument) {
        document.appSettings?.applyTo(appSettingsRepository)
    }

    private fun ConfigurationWifiProfile.toEntity(): WifiProfileEntity {
        return WifiProfileEntity(
            id = id,
            name = name,
            ssid = ssid,
            connectionMode = connectionMode,
            securityType = securityType,
            securityTypeVerifiedLocally = false
        )
    }

    private fun ConfigurationDevice.toEntity(sortOrder: Int): DeviceEntity {
        return DeviceEntity(
            id = id,
            name = name,
            actionLabel = actionLabel,
            apiProtocol = action.protocol,
            apiMethod = action.method,
            apiPath = action.path,
            apiRequestBody = action.requestBody.orEmpty(),
            apiContentType = action.contentType,
            sortOrder = sortOrder,
            shortcutEnabled = shortcutEnabled
        )
    }

    private fun ConfigurationDocument.toConnectionEntities(): List<DeviceConnectionEntity> {
        return devices.flatMap { device ->
            device.connections.mapIndexed { index, connection ->
                DeviceConnectionEntity(
                    id = "${device.id}:${connection.wifiProfileId}",
                    deviceId = device.id,
                    wifiProfileId = connection.wifiProfileId,
                    host = connection.host,
                    priority = index
                )
            }
        }
    }

    private fun ConfigurationSwitchGroup.toEntity(sortOrder: Int): SwitchGroupEntity {
        return SwitchGroupEntity(
            id = id,
            name = name,
            actionLabel = actionLabel,
            sortOrder = sortOrder,
            errorStrategy = errorStrategy
        )
    }

    private fun ConfigurationDocument.toSwitchGroupMemberEntities(): List<SwitchGroupMemberEntity> {
        return switchGroups.flatMap { group ->
            group.members.mapIndexed { index, member ->
                SwitchGroupMemberEntity(
                    id = member.id,
                    groupId = group.id,
                    deviceId = member.deviceId,
                    sortOrder = index,
                    pauseAfterMillis = member.pauseAfterMillis
                )
            }
        }
    }

    private fun ConfigurationDocument.passwordCount(): Int {
        return wifiProfiles.count { it.isPasswordPresent && !it.password.isNullOrEmpty() }
    }

    private fun ConfigurationDocument.passwordDeletionCount(): Int {
        return wifiProfiles.count { it.isPasswordPresent && it.password.isNullOrEmpty() }
    }

    private fun readLimitedUtf8(input: InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0

        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                break
            }
            totalBytes += read
            require(totalBytes <= MAX_IMPORT_BYTES) {
                "Importdatei ist größer als 1 MiB"
            }
            output.write(buffer, 0, read)
        }
        return output.toString(StandardCharsets.UTF_8.name())
    }

    private fun fetchImportJson(url: HttpUrl): String {
        val firstResponse = executeImportGet(url)
        firstResponse.use { response ->
            if (response.request.url.host == GOOGLE_ACCOUNTS_HOST) {
                throw IllegalArgumentException(
                    "Die Google-Drive-Datei ist nicht öffentlich freigegeben. " +
                        "Aktiviere „Jeder mit dem Link“."
                )
            }
            if (!response.isSuccessful) {
                if (response.code == 401 && url.host.endsWith(GOOGLE_DRIVE_HOST)) {
                    throw IllegalArgumentException(
                        "Die Google-Drive-Datei ist nicht öffentlich freigegeben. " +
                            "Aktiviere „Jeder mit dem Link“."
                    )
                }
                throw IllegalArgumentException("Import-URL antwortet mit HTTP ${response.code}")
            }

            if (!response.isHtmlResponse()) {
                return response.body.byteStream().use(::readLimitedUtf8)
            }
        }

        val downloadUrl = nextcloudDownloadUrl(url)
            ?: throw IllegalArgumentException(
                "Die URL liefert eine Webseite statt einer SwitchWerk-JSON-Datei"
            )
        executeImportGet(downloadUrl).use { response ->
            if (!response.isSuccessful) {
                throw IllegalArgumentException("Download-URL antwortet mit HTTP ${response.code}")
            }
            if (response.isHtmlResponse()) {
                throw IllegalArgumentException(
                    "Der Freigabelink liefert keine SwitchWerk-JSON-Datei"
                )
            }
            return response.body.byteStream().use(::readLimitedUtf8)
        }
    }

    private fun executeImportGet(url: HttpUrl) = httpClient.newCall(
        Request.Builder()
            .url(url)
            .get()
            .build()
    ).execute().also { response ->
        if (url.isHttps && !response.request.url.isHttps) {
            response.close()
            throw IllegalArgumentException(
                "Weiterleitung auf eine unsichere HTTP-URL wurde abgelehnt"
            )
        }
    }

    private fun okhttp3.Response.isHtmlResponse(): Boolean {
        return header("Content-Type")
            ?.substringBefore(';')
            ?.trim()
            ?.equals("text/html", ignoreCase = true) == true
    }

    private companion object {
        const val MAX_IMPORT_BYTES = 1024 * 1024
        const val GOOGLE_ACCOUNTS_HOST = "accounts.google.com"
        const val GOOGLE_DRIVE_HOST = "google.com"
    }
}

internal fun AppSettings.toConfigurationAppSettings(): ConfigurationAppSettings {
    return ConfigurationAppSettings(
        themeMode = themeMode.name,
        showActionDetails = showActionDetails,
        detailPanelHeight = detailPanelHeight.name,
        diagnosticsNewestFirst = diagnosticsNewestFirst,
        dashboardLayoutMode = dashboardLayoutMode.name,
        language = language.name,
        wifiProfileSortCriterion = wifiProfileSortCriterion.name,
        wifiProfileSortDirection = wifiProfileSortDirection.name,
        externalIntentsEnabled = externalIntentsEnabled
    )
}

internal fun ConfigurationAppSettings.applyTo(repository: AppSettingsRepository) {
    repository.setThemeMode(AppThemeMode.valueOf(themeMode))
    repository.setShowActionDetails(showActionDetails)
    repository.setDetailPanelHeight(DetailPanelHeight.valueOf(detailPanelHeight))
    repository.setDiagnosticsNewestFirst(diagnosticsNewestFirst)
    dashboardLayoutMode?.let { repository.setDashboardLayoutMode(DashboardLayoutMode.valueOf(it)) }
    language?.let { repository.setLanguage(AppLanguage.valueOf(it)) }
    if (wifiProfileSortCriterion != null && wifiProfileSortDirection != null) {
        repository.setWifiProfileSorting(
            WifiProfileSortCriterion.valueOf(wifiProfileSortCriterion),
            WifiProfileSortDirection.valueOf(wifiProfileSortDirection)
        )
    }
    externalIntentsEnabled?.let(repository::setExternalIntentsEnabled)
}

internal fun ConfigurationAppSettings?.externalIntentsChangeFrom(
    currentSettings: AppSettings
): Boolean? {
    return this?.externalIntentsEnabled?.takeIf { it != currentSettings.externalIntentsEnabled }
}

internal fun importHttpUrl(url: String): HttpUrl {
    val httpUrl = url.trim().toHttpUrlOrNull()
    require(httpUrl != null && httpUrl.host.isNotBlank()) {
        "Für den URL-Import ist eine gültige HTTP/HTTPS-URL erforderlich"
    }
    return httpUrl
}

internal fun ConfigurationDocument.normalizedForImport(): ConfigurationDocument {
    return copy(
        wifiProfiles = wifiProfiles.map { profile ->
            profile.copy(
                name = profile.name.trim(),
                ssid = profile.ssid.trim()
            )
        },
        switchGroups = switchGroups.map { group ->
            group.copy(
                name = group.name.trim(),
                actionLabel = group.actionLabel.trim()
            )
        }
    )
}

internal fun nextcloudDownloadUrl(url: HttpUrl): HttpUrl? {
    val segments = url.pathSegments.filter { it.isNotBlank() }
    val shareSegmentIndex = segments.indexOfLast { it == "s" }
    val hasShareToken = shareSegmentIndex >= 0 && shareSegmentIndex == segments.lastIndex - 1
    if (!hasShareToken) {
        return null
    }
    return url.newBuilder()
        .addPathSegment("download")
        .build()
}

internal fun googleDriveDownloadUrl(url: HttpUrl): HttpUrl? {
    if (url.host != "drive.google.com") {
        return null
    }
    val segments = url.pathSegments.filter { it.isNotBlank() }
    val fileSegmentIndex = segments.indexOf("d")
    val fileId = segments.getOrNull(fileSegmentIndex + 1)
        ?.takeIf { fileSegmentIndex > 0 && segments[fileSegmentIndex - 1] == "file" }
        ?: return null

    return HttpUrl.Builder()
        .scheme("https")
        .host("drive.usercontent.google.com")
        .addPathSegment("download")
        .addQueryParameter("id", fileId)
        .addQueryParameter("export", "download")
        .addQueryParameter("confirm", "t")
        .build()
}
