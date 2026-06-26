package de.piecha.switchwerk.data.update

import de.piecha.switchwerk.domain.model.AppUpdateError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseEvaluatorTest {
    private val evaluator = GitHubReleaseEvaluator()

    @Test
    fun regularReleaseWithMatchingAssetIsAccepted() {
        val result = evaluator.evaluate(
            listOf(
                release(
                    tagName = "v0.7.0",
                    prerelease = true,
                    assets = listOf(asset("SwitchWerk-0.7.0.apk"))
                ),
                release(
                    tagName = "v0.8.0",
                    assets = listOf(asset("SwitchWerk-0.8.0.apk"))
                )
            )
        )

        assertTrue(result is ReleaseEvaluationResult.Success)
        val release = (result as ReleaseEvaluationResult.Success).release
        assertEquals("0.8.0", release.version)
        assertEquals("SwitchWerk-0.8.0.apk", release.apkAsset.name)
    }

    @Test
    fun missingAssetIsReported() {
        val result = evaluator.evaluate(
            listOf(release(tagName = "v0.8.0", assets = listOf(asset("SwitchWerk.apk"))))
        )

        assertEquals(
            AppUpdateError.MissingApkAsset,
            (result as ReleaseEvaluationResult.Error).error
        )
    }

    @Test
    fun ambiguousAssetIsReported() {
        val result = evaluator.evaluate(
            listOf(
                release(
                    tagName = "v0.8.0",
                    assets = listOf(
                        asset("SwitchWerk-0.8.0.apk"),
                        asset("SwitchWerk-0.8.0.apk")
                    )
                )
            )
        )

        assertEquals(
            AppUpdateError.AmbiguousApkAsset,
            (result as ReleaseEvaluationResult.Error).error
        )
    }

    private fun release(
        tagName: String,
        prerelease: Boolean = false,
        assets: List<GitHubReleaseAsset>
    ): GitHubRelease = GitHubRelease(
        tagName = tagName,
        name = "SwitchWerk ${tagName.removePrefix("v")}",
        body = "Notes",
        htmlUrl = "https://github.com/spi43984/SwitchWerk/releases/tag/$tagName",
        draft = false,
        prerelease = prerelease,
        assets = assets
    )

    private fun asset(name: String): GitHubReleaseAsset = GitHubReleaseAsset(
        name = name,
        downloadUrl = "https://github.com/spi43984/SwitchWerk/releases/download/v0.8.0/$name",
        sizeBytes = 123L
    )
}
