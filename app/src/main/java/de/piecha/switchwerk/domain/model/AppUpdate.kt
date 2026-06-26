package de.piecha.switchwerk.domain.model

import android.net.Uri

data class AppVersion(
    val value: String
)

data class AppReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long?
)

data class AppRelease(
    val version: String,
    val title: String,
    val notes: String,
    val htmlUrl: String,
    val apkAsset: AppReleaseAsset
)

data class AppUpdateSnapshot(
    val installedVersion: String,
    val availableRelease: AppRelease? = null,
    val isUpdateAvailable: Boolean = false,
    val isDebugBuild: Boolean = false,
    val lastCheckedAtMillis: Long? = null,
    val lastSuccessfulCheckDate: String? = null,
    val error: AppUpdateError? = null,
    val downloadedApkUri: Uri? = null
)

sealed interface AppUpdateError {
    data object DebugBuild : AppUpdateError
    data object NoRegularRelease : AppUpdateError
    data object MissingApkAsset : AppUpdateError
    data object AmbiguousApkAsset : AppUpdateError
    data object InvalidReleaseData : AppUpdateError
    data object Network : AppUpdateError
    data object GitHub : AppUpdateError
    data object Download : AppUpdateError
    data object Install : AppUpdateError
}

sealed interface AppUpdateCheckResult {
    data class Success(val snapshot: AppUpdateSnapshot) : AppUpdateCheckResult
    data class Error(val snapshot: AppUpdateSnapshot) : AppUpdateCheckResult
}

sealed interface AppUpdateDownloadState {
    data object Idle : AppUpdateDownloadState
    data object Started : AppUpdateDownloadState
    data class Progress(val percent: Int) : AppUpdateDownloadState
    data class Completed(val apkUri: Uri) : AppUpdateDownloadState
    data class Failed(val error: AppUpdateError) : AppUpdateDownloadState
}
