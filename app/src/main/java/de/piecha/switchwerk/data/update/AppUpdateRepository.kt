package de.piecha.switchwerk.data.update

import android.net.Uri
import de.piecha.switchwerk.domain.model.AppUpdateCheckResult
import de.piecha.switchwerk.domain.model.AppUpdateDownloadState
import de.piecha.switchwerk.domain.model.AppUpdateError
import de.piecha.switchwerk.domain.model.AppUpdateSnapshot
import java.time.LocalDate

interface AppUpdateRepository {
    fun cachedUpdate(): AppUpdateSnapshot
    suspend fun checkForUpdates(force: Boolean): AppUpdateCheckResult
    suspend fun downloadUpdate(
        onProgress: (AppUpdateDownloadState) -> Unit
    ): AppUpdateDownloadState
}

class DefaultAppUpdateRepository(
    private val releaseService: GitHubReleaseService,
    private val releaseEvaluator: GitHubReleaseEvaluator,
    private val cacheRepository: AppUpdateCacheRepository,
    private val downloadService: AppUpdateDownloadService,
    private val installedVersion: String,
    private val isDebugBuild: Boolean,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() }
) : AppUpdateRepository {
    override fun cachedUpdate(): AppUpdateSnapshot {
        return cacheRepository.load(installedVersion, isDebugBuild)
    }

    override suspend fun checkForUpdates(force: Boolean): AppUpdateCheckResult {
        val cached = cachedUpdate()
        if (isDebugBuild) {
            val snapshot = cached.copy(
                installedVersion = installedVersion,
                isDebugBuild = true,
                isUpdateAvailable = false,
                error = AppUpdateError.DebugBuild
            )
            cacheRepository.save(snapshot)
            return AppUpdateCheckResult.Error(snapshot)
        }

        val currentDate = today().toString()
        if (!force && cached.lastSuccessfulCheckDate == currentDate) {
            return AppUpdateCheckResult.Success(cached)
        }

        val releases = releaseService.releases().getOrElse { error ->
            val snapshot = cached.copy(
                lastCheckedAtMillis = currentTimeMillis(),
                error = if (error is GitHubReleaseHttpException) {
                    AppUpdateError.GitHub
                } else {
                    AppUpdateError.Network
                }
            )
            cacheRepository.save(snapshot)
            return AppUpdateCheckResult.Error(snapshot)
        }

        val evaluation = releaseEvaluator.evaluate(releases)
        val snapshot = when (evaluation) {
            is ReleaseEvaluationResult.Success -> AppUpdateSnapshot(
                installedVersion = installedVersion,
                availableRelease = evaluation.release,
                isUpdateAvailable = AppVersionComparator.isNewer(
                    evaluation.release.version,
                    installedVersion
                ),
                isDebugBuild = false,
                lastCheckedAtMillis = currentTimeMillis(),
                lastSuccessfulCheckDate = currentDate,
                error = null,
                downloadedApkUri = cached.downloadedApkUri
            )
            is ReleaseEvaluationResult.Error -> cached.copy(
                lastCheckedAtMillis = currentTimeMillis(),
                lastSuccessfulCheckDate = currentDate,
                error = evaluation.error,
                downloadedApkUri = null
            )
        }
        cacheRepository.save(snapshot)

        return if (snapshot.error == null) {
            AppUpdateCheckResult.Success(snapshot)
        } else {
            AppUpdateCheckResult.Error(snapshot)
        }
    }

    override suspend fun downloadUpdate(
        onProgress: (AppUpdateDownloadState) -> Unit
    ): AppUpdateDownloadState {
        val release = cachedUpdate().availableRelease
            ?: return AppUpdateDownloadState.Failed(AppUpdateError.MissingApkAsset)
        val result = downloadService.download(release, onProgress)
        if (result is AppUpdateDownloadState.Completed) {
            cacheRepository.save(cachedUpdate().copy(downloadedApkUri = result.apkUri, error = null))
        } else if (result is AppUpdateDownloadState.Failed) {
            cacheRepository.save(cachedUpdate().copy(error = result.error))
        }
        return result
    }
}

interface AppUpdateDownloadService {
    suspend fun download(
        release: de.piecha.switchwerk.domain.model.AppRelease,
        onProgress: (AppUpdateDownloadState) -> Unit
    ): AppUpdateDownloadState
}
