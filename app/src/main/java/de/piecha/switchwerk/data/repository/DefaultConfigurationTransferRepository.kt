package de.piecha.switchwerk.data.repository

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import de.piecha.switchwerk.data.local.AppDatabase
import de.piecha.switchwerk.data.local.dao.DeviceConnectionDao
import de.piecha.switchwerk.data.local.dao.DeviceDao
import de.piecha.switchwerk.data.local.dao.WifiProfileDao
import de.piecha.switchwerk.data.local.entity.DeviceConnectionEntity
import de.piecha.switchwerk.data.local.entity.DeviceEntity
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.data.transfer.CONFIGURATION_SCHEMA_VERSION
import de.piecha.switchwerk.data.transfer.ConfigurationDevice
import de.piecha.switchwerk.data.transfer.ConfigurationDeviceAction
import de.piecha.switchwerk.data.transfer.ConfigurationDeviceConnection
import de.piecha.switchwerk.data.transfer.ConfigurationDocument
import de.piecha.switchwerk.data.transfer.ConfigurationImportValidator
import de.piecha.switchwerk.data.transfer.ConfigurationJsonCodec
import de.piecha.switchwerk.data.transfer.ConfigurationWifiProfile
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.HttpUrl
import okhttp3.Request

class DefaultConfigurationTransferRepository(
    private val contentResolver: ContentResolver,
    private val database: AppDatabase,
    private val deviceDao: DeviceDao,
    private val deviceConnectionDao: DeviceConnectionDao,
    private val wifiProfileDao: WifiProfileDao,
    private val credentialStore: WifiCredentialStore,
    private val httpClient: OkHttpClient,
    private val jsonCodec: ConfigurationJsonCodec,
    private val validator: ConfigurationImportValidator
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
            val uri = runCatching { Uri.parse(url.trim()) }
                .getOrElse { throw IllegalArgumentException("URL ist ungültig") }
            require(uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()) {
                "Für den URL-Import ist eine gültige HTTPS-URL erforderlich"
            }

            val requestUrl = Request.Builder()
                .url(uri.toString())
                .build()
                .url
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
        mode: ConfigurationImportMode
    ) {
        withContext(Dispatchers.IO) {
            validator.validate(preparedImport.document)
            val oldWifiProfileIds = wifiProfileDao.getAll().map { it.id }

            database.withTransaction {
                when (mode) {
                    ConfigurationImportMode.REPLACE -> replaceConfiguration(preparedImport.document)
                    ConfigurationImportMode.MERGE -> mergeConfiguration(preparedImport.document)
                }
            }

            if (mode == ConfigurationImportMode.REPLACE) {
                oldWifiProfileIds.forEach { credentialStore.deletePassword(it) }
            }
            applyImportedPasswords(preparedImport.document)
        }
    }

    private suspend fun buildExportDocument(includePasswords: Boolean): ConfigurationDocument {
        val profiles = wifiProfileDao.getAll()
        val devices = deviceDao.getAll()
        val connections = deviceConnectionDao.getAll()

        return ConfigurationDocument(
            schemaVersion = CONFIGURATION_SCHEMA_VERSION,
            wifiProfiles = profiles.map { profile ->
                ConfigurationWifiProfile(
                    id = profile.id,
                    name = profile.name,
                    ssid = profile.ssid,
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
                    action = ConfigurationDeviceAction(
                        method = device.apiMethod,
                        path = device.apiPath
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
            }
        validator.validate(document)

        val existingWifiProfileIds = wifiProfileDao.getAll().map { it.id }.toSet()
        val existingDeviceIds = deviceDao.getAll().map { it.id }.toSet()
        val importedWifiProfileIds = document.wifiProfiles.map { it.id }.toSet()
        val importedDeviceIds = document.devices.map { it.id }.toSet()

        val summary = when (mode) {
            ConfigurationImportMode.MERGE -> ConfigurationImportSummary(
                wifiProfilesNew = importedWifiProfileIds.count { it !in existingWifiProfileIds },
                wifiProfilesOverwritten = importedWifiProfileIds.count { it in existingWifiProfileIds },
                devicesNew = importedDeviceIds.count { it !in existingDeviceIds },
                devicesOverwritten = importedDeviceIds.count { it in existingDeviceIds },
                passwordsIncluded = document.passwordCount(),
                passwordsDeleted = document.passwordDeletionCount(),
                localWifiProfilesDeleted = 0,
                localDevicesDeleted = 0
            )

            ConfigurationImportMode.REPLACE -> ConfigurationImportSummary(
                wifiProfilesNew = document.wifiProfiles.size,
                wifiProfilesOverwritten = 0,
                devicesNew = document.devices.size,
                devicesOverwritten = 0,
                passwordsIncluded = document.passwordCount(),
                passwordsDeleted = document.passwordDeletionCount(),
                localWifiProfilesDeleted = existingWifiProfileIds.size,
                localDevicesDeleted = existingDeviceIds.size
            )
        }

        return PreparedConfigurationImport(
            document = document,
            summary = summary
        )
    }

    private suspend fun replaceConfiguration(document: ConfigurationDocument) {
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
    }

    private suspend fun mergeConfiguration(document: ConfigurationDocument) {
        val existingDevices = deviceDao.getAll()
        val existingSortOrders = existingDevices.associate { it.id to it.sortOrder }
        var nextSortOrder = (existingDevices.maxOfOrNull { it.sortOrder } ?: -1) + 1

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

    private fun ConfigurationWifiProfile.toEntity(): WifiProfileEntity {
        return WifiProfileEntity(
            id = id,
            name = name,
            ssid = ssid,
            securityType = securityType
        )
    }

    private fun ConfigurationDevice.toEntity(sortOrder: Int): DeviceEntity {
        return DeviceEntity(
            id = id,
            name = name,
            actionLabel = actionLabel,
            apiMethod = action.method,
            apiPath = action.path,
            sortOrder = sortOrder
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
        val firstResponse = executeHttpsGet(url)
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
        executeHttpsGet(downloadUrl).use { response ->
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

    private fun executeHttpsGet(url: HttpUrl) = httpClient.newCall(
        Request.Builder()
            .url(url)
            .get()
            .build()
    ).execute().also { response ->
        if (!response.request.url.isHttps) {
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
