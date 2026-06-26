package de.piecha.switchwerk.data.update

import android.content.Context
import android.net.Uri
import de.piecha.switchwerk.domain.model.AppRelease
import de.piecha.switchwerk.domain.model.AppReleaseAsset
import de.piecha.switchwerk.domain.model.AppUpdateError
import de.piecha.switchwerk.domain.model.AppUpdateSnapshot

interface AppUpdateCacheRepository {
    fun load(installedVersion: String, isDebugBuild: Boolean): AppUpdateSnapshot
    fun save(snapshot: AppUpdateSnapshot)
}

class SharedPreferencesAppUpdateCacheRepository(context: Context) : AppUpdateCacheRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun load(installedVersion: String, isDebugBuild: Boolean): AppUpdateSnapshot {
        val releaseVersion = preferences.getString(KEY_RELEASE_VERSION, null)
        val assetName = preferences.getString(KEY_ASSET_NAME, null)
        val assetUrl = preferences.getString(KEY_ASSET_URL, null)
        val release = if (releaseVersion != null && assetName != null && assetUrl != null) {
            AppRelease(
                version = releaseVersion,
                title = preferences.getString(KEY_RELEASE_TITLE, null).orEmpty(),
                notes = preferences.getString(KEY_RELEASE_NOTES, null).orEmpty(),
                htmlUrl = preferences.getString(KEY_RELEASE_HTML_URL, null).orEmpty(),
                apkAsset = AppReleaseAsset(
                    name = assetName,
                    downloadUrl = assetUrl,
                    sizeBytes = preferences.getLong(KEY_ASSET_SIZE, -1L).takeIf { it > 0L }
                )
            )
        } else {
            null
        }

        return AppUpdateSnapshot(
            installedVersion = installedVersion,
            availableRelease = release,
            isUpdateAvailable = release?.let {
                AppVersionComparator.isNewer(it.version, installedVersion) && !isDebugBuild
            } ?: false,
            isDebugBuild = isDebugBuild,
            lastCheckedAtMillis = preferences.getLong(KEY_LAST_CHECKED_AT, 0L).takeIf { it > 0L },
            lastSuccessfulCheckDate = preferences.getString(KEY_LAST_SUCCESSFUL_CHECK_DATE, null),
            error = preferences.getString(KEY_ERROR, null)?.toUpdateError(),
            downloadedApkUri = preferences.getString(KEY_DOWNLOADED_APK_URI, null)?.let(Uri::parse)
        )
    }

    override fun save(snapshot: AppUpdateSnapshot) {
        preferences.edit()
            .putString(KEY_RELEASE_VERSION, snapshot.availableRelease?.version)
            .putString(KEY_RELEASE_TITLE, snapshot.availableRelease?.title)
            .putString(KEY_RELEASE_NOTES, snapshot.availableRelease?.notes)
            .putString(KEY_RELEASE_HTML_URL, snapshot.availableRelease?.htmlUrl)
            .putString(KEY_ASSET_NAME, snapshot.availableRelease?.apkAsset?.name)
            .putString(KEY_ASSET_URL, snapshot.availableRelease?.apkAsset?.downloadUrl)
            .putLong(KEY_ASSET_SIZE, snapshot.availableRelease?.apkAsset?.sizeBytes ?: -1L)
            .putLong(KEY_LAST_CHECKED_AT, snapshot.lastCheckedAtMillis ?: 0L)
            .putString(KEY_LAST_SUCCESSFUL_CHECK_DATE, snapshot.lastSuccessfulCheckDate)
            .putString(KEY_ERROR, snapshot.error?.toCacheValue())
            .putString(KEY_DOWNLOADED_APK_URI, snapshot.downloadedApkUri?.toString())
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "app_update_cache"
        const val KEY_RELEASE_VERSION = "release_version"
        const val KEY_RELEASE_TITLE = "release_title"
        const val KEY_RELEASE_NOTES = "release_notes"
        const val KEY_RELEASE_HTML_URL = "release_html_url"
        const val KEY_ASSET_NAME = "asset_name"
        const val KEY_ASSET_URL = "asset_url"
        const val KEY_ASSET_SIZE = "asset_size"
        const val KEY_LAST_CHECKED_AT = "last_checked_at"
        const val KEY_LAST_SUCCESSFUL_CHECK_DATE = "last_successful_check_date"
        const val KEY_ERROR = "error"
        const val KEY_DOWNLOADED_APK_URI = "downloaded_apk_uri"
    }
}

private fun AppUpdateError.toCacheValue(): String = when (this) {
    AppUpdateError.DebugBuild -> "debug_build"
    AppUpdateError.NoRegularRelease -> "no_regular_release"
    AppUpdateError.MissingApkAsset -> "missing_apk_asset"
    AppUpdateError.AmbiguousApkAsset -> "ambiguous_apk_asset"
    AppUpdateError.InvalidReleaseData -> "invalid_release_data"
    AppUpdateError.Network -> "network"
    AppUpdateError.GitHub -> "github"
    AppUpdateError.Download -> "download"
    AppUpdateError.Install -> "install"
}

private fun String.toUpdateError(): AppUpdateError? = when (this) {
    "debug_build" -> AppUpdateError.DebugBuild
    "no_regular_release" -> AppUpdateError.NoRegularRelease
    "missing_apk_asset" -> AppUpdateError.MissingApkAsset
    "ambiguous_apk_asset" -> AppUpdateError.AmbiguousApkAsset
    "invalid_release_data" -> AppUpdateError.InvalidReleaseData
    "network" -> AppUpdateError.Network
    "github" -> AppUpdateError.GitHub
    "download" -> AppUpdateError.Download
    "install" -> AppUpdateError.Install
    else -> null
}
