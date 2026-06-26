package de.piecha.switchwerk.data.update

data class GitHubReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long?
)

data class GitHubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val htmlUrl: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val assets: List<GitHubReleaseAsset>
)
