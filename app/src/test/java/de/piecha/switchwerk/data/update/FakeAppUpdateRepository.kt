package de.piecha.switchwerk.data.update

import de.piecha.switchwerk.domain.model.AppUpdateCheckResult
import de.piecha.switchwerk.domain.model.AppUpdateDownloadState
import de.piecha.switchwerk.domain.model.AppUpdateSnapshot

class FakeAppUpdateRepository(
    private var snapshot: AppUpdateSnapshot = AppUpdateSnapshot(installedVersion = "0.7.0")
) : AppUpdateRepository {
    var forceValues = mutableListOf<Boolean>()

    override fun cachedUpdate(): AppUpdateSnapshot = snapshot

    override suspend fun checkForUpdates(force: Boolean): AppUpdateCheckResult {
        forceValues += force
        return AppUpdateCheckResult.Success(snapshot)
    }

    override suspend fun downloadUpdate(
        onProgress: (AppUpdateDownloadState) -> Unit
    ): AppUpdateDownloadState = AppUpdateDownloadState.Idle
}
