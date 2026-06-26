package de.piecha.switchwerk.data.update

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

interface GitHubReleaseService {
    suspend fun releases(): Result<List<GitHubRelease>>
}

class GitHubReleaseHttpException(
    val statusCode: Int
) : IOException("GitHub returned HTTP $statusCode")

class OkHttpGitHubReleaseService(
    private val client: OkHttpClient,
    private val releasesUrl: String = RELEASES_URL
) : GitHubReleaseService {
    override suspend fun releases(): Result<List<GitHubRelease>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(releasesUrl)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "SwitchWerk")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw GitHubReleaseHttpException(response.code)
                }
                parseReleases(response.body.string())
            }
        }
    }

    private fun parseReleases(body: String): List<GitHubRelease> {
        val releases = JSONArray(body)
        return List(releases.length()) { index ->
            val release = releases.getJSONObject(index)
            val assets = release.optJSONArray("assets") ?: JSONArray()
            GitHubRelease(
                tagName = release.optString("tag_name"),
                name = release.optString("name"),
                body = release.optString("body"),
                htmlUrl = release.optString("html_url"),
                draft = release.optBoolean("draft"),
                prerelease = release.optBoolean("prerelease"),
                assets = List(assets.length()) { assetIndex ->
                    val asset = assets.getJSONObject(assetIndex)
                    GitHubReleaseAsset(
                        name = asset.optString("name"),
                        downloadUrl = asset.optString("browser_download_url"),
                        sizeBytes = asset.optLong("size").takeIf { it > 0L }
                    )
                }
            )
        }
    }

    private companion object {
        const val RELEASES_URL = "https://api.github.com/repos/spi43984/SwitchWerk/releases"
    }
}
