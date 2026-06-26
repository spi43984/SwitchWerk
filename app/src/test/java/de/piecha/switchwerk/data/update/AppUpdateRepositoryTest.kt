package de.piecha.switchwerk.data.update

import de.piecha.switchwerk.domain.model.AppRelease
import de.piecha.switchwerk.domain.model.AppUpdateCheckResult
import de.piecha.switchwerk.domain.model.AppUpdateDownloadState
import de.piecha.switchwerk.domain.model.AppUpdateSnapshot
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateRepositoryTest {
    @Test
    fun automaticCheckUsesSuccessfulCacheForSameCalendarDay() = runBlocking {
        val service = FakeGitHubReleaseService(
            releases = listOf(release("v0.8.0", listOf(asset("SwitchWerk-0.8.0.apk"))))
        )
        val cache = FakeAppUpdateCacheRepository(
            AppUpdateSnapshot(
                installedVersion = "0.7.0",
                lastSuccessfulCheckDate = "2026-06-26"
            )
        )
        val repository = repository(service, cache)

        val result = repository.checkForUpdates(force = false)

        assertTrue(result is AppUpdateCheckResult.Success)
        assertEquals(0, service.callCount)
    }

    @Test
    fun manualCheckBypassesDailyCache() = runBlocking {
        val service = FakeGitHubReleaseService(
            releases = listOf(release("v0.8.0", listOf(asset("SwitchWerk-0.8.0.apk"))))
        )
        val cache = FakeAppUpdateCacheRepository(
            AppUpdateSnapshot(
                installedVersion = "0.7.0",
                lastSuccessfulCheckDate = "2026-06-26"
            )
        )
        val repository = repository(service, cache)

        val result = repository.checkForUpdates(force = true)

        assertTrue(result is AppUpdateCheckResult.Success)
        assertEquals(1, service.callCount)
        assertTrue((result as AppUpdateCheckResult.Success).snapshot.isUpdateAvailable)
    }

    @Test
    fun debugBuildIsNeverUpdateable() = runBlocking {
        val service = FakeGitHubReleaseService(
            releases = listOf(release("v9.0.0", listOf(asset("SwitchWerk-9.0.0.apk"))))
        )
        val repository = repository(
            service = service,
            cache = FakeAppUpdateCacheRepository(),
            isDebugBuild = true
        )

        val result = repository.checkForUpdates(force = true)

        assertTrue(result is AppUpdateCheckResult.Error)
        assertFalse((result as AppUpdateCheckResult.Error).snapshot.isUpdateAvailable)
        assertEquals(0, service.callCount)
    }

    private fun repository(
        service: FakeGitHubReleaseService,
        cache: FakeAppUpdateCacheRepository,
        isDebugBuild: Boolean = false
    ): DefaultAppUpdateRepository = DefaultAppUpdateRepository(
        releaseService = service,
        releaseEvaluator = GitHubReleaseEvaluator(),
        cacheRepository = cache,
        downloadService = object : AppUpdateDownloadService {
            override suspend fun download(
                release: AppRelease,
                onProgress: (AppUpdateDownloadState) -> Unit
            ): AppUpdateDownloadState = AppUpdateDownloadState.Failed(
                de.piecha.switchwerk.domain.model.AppUpdateError.Download
            )
        },
        installedVersion = "0.7.0",
        isDebugBuild = isDebugBuild,
        today = { LocalDate.of(2026, 6, 26) },
        currentTimeMillis = { 1_772_000_000_000L }
    )

    private fun release(
        tagName: String,
        assets: List<GitHubReleaseAsset>
    ): GitHubRelease = GitHubRelease(
        tagName = tagName,
        name = "SwitchWerk ${tagName.removePrefix("v")}",
        body = "Notes",
        htmlUrl = "https://github.com/spi43984/SwitchWerk/releases/tag/$tagName",
        draft = false,
        prerelease = false,
        assets = assets
    )

    private fun asset(name: String): GitHubReleaseAsset = GitHubReleaseAsset(
        name = name,
        downloadUrl = "https://github.com/spi43984/SwitchWerk/releases/download/v0.8.0/$name",
        sizeBytes = 123L
    )

    private class FakeGitHubReleaseService(
        private val releases: List<GitHubRelease>
    ) : GitHubReleaseService {
        var callCount = 0

        override suspend fun releases(): Result<List<GitHubRelease>> {
            callCount += 1
            return Result.success(releases)
        }
    }

    private class FakeAppUpdateCacheRepository(
        initialSnapshot: AppUpdateSnapshot = AppUpdateSnapshot(installedVersion = "0.7.0")
    ) : AppUpdateCacheRepository {
        private var snapshot = initialSnapshot

        override fun load(installedVersion: String, isDebugBuild: Boolean): AppUpdateSnapshot {
            return snapshot.copy(installedVersion = installedVersion, isDebugBuild = isDebugBuild)
        }

        override fun save(snapshot: AppUpdateSnapshot) {
            this.snapshot = snapshot
        }
    }
}
