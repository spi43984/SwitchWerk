package de.piecha.switchwerk.data.update

import de.piecha.switchwerk.domain.model.AppRelease
import de.piecha.switchwerk.domain.model.AppReleaseAsset
import de.piecha.switchwerk.domain.model.AppUpdateError

sealed interface ReleaseEvaluationResult {
    data class Success(val release: AppRelease) : ReleaseEvaluationResult
    data class Error(val error: AppUpdateError) : ReleaseEvaluationResult
}

class GitHubReleaseEvaluator {
    fun evaluate(releases: List<GitHubRelease>): ReleaseEvaluationResult {
        val release = releases.firstOrNull { !it.draft && !it.prerelease }
            ?: return ReleaseEvaluationResult.Error(AppUpdateError.NoRegularRelease)

        val version = release.tagName
            .ifBlank { release.name }
            .trim()
            .removePrefix("v")
            .removePrefix("V")

        if (version.isBlank() || release.htmlUrl.isBlank()) {
            return ReleaseEvaluationResult.Error(AppUpdateError.InvalidReleaseData)
        }

        val expectedAssetName = "SwitchWerk-$version.apk"
        val matchingAssets = release.assets.filter { it.name == expectedAssetName }

        return when {
            matchingAssets.isEmpty() -> ReleaseEvaluationResult.Error(AppUpdateError.MissingApkAsset)
            matchingAssets.size > 1 -> ReleaseEvaluationResult.Error(AppUpdateError.AmbiguousApkAsset)
            matchingAssets.single().downloadUrl.isBlank() ->
                ReleaseEvaluationResult.Error(AppUpdateError.InvalidReleaseData)
            else -> {
                val asset = matchingAssets.single()
                ReleaseEvaluationResult.Success(
                    AppRelease(
                        version = version,
                        title = release.name.ifBlank { "SwitchWerk $version" },
                        notes = release.body,
                        htmlUrl = release.htmlUrl,
                        apkAsset = AppReleaseAsset(
                            name = asset.name,
                            downloadUrl = asset.downloadUrl,
                            sizeBytes = asset.sizeBytes
                        )
                    )
                )
            }
        }
    }
}
