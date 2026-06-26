package de.piecha.switchwerk.data.update

import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import de.piecha.switchwerk.domain.model.AppRelease
import de.piecha.switchwerk.domain.model.AppUpdateDownloadState
import de.piecha.switchwerk.domain.model.AppUpdateError
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class AndroidAppUpdateDownloadService(
    private val context: Context,
    private val client: OkHttpClient
) : AppUpdateDownloadService {
    override suspend fun download(
        release: AppRelease,
        onProgress: (AppUpdateDownloadState) -> Unit
    ): AppUpdateDownloadState = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(AppUpdateDownloadState.Started)
            val request = Request.Builder()
                .url(release.apkAsset.downloadUrl)
                .header("User-Agent", "SwitchWerk")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext AppUpdateDownloadState.Failed(AppUpdateError.Download)
                }
                val body = response.body
                val apkFile = targetFile(release)
                apkFile.parentFile?.mkdirs()
                val totalBytes = body.contentLength().takeIf { it > 0L }
                    ?: release.apkAsset.sizeBytes
                var readBytes = 0L
                apkFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val count = input.read(buffer)
                            if (count == -1) {
                                break
                            }
                            output.write(buffer, 0, count)
                            readBytes += count
                            totalBytes?.let { total ->
                                val percent = ((readBytes * 100L) / total)
                                    .toInt()
                                    .coerceIn(0, 100)
                                onProgress(AppUpdateDownloadState.Progress(percent))
                            }
                        }
                    }
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                AppUpdateDownloadState.Completed(uri)
            }
        }.getOrElse { error ->
            if (error is IOException || error is IllegalArgumentException) {
                AppUpdateDownloadState.Failed(AppUpdateError.Download)
            } else {
                throw error
            }
        }
    }

    private fun targetFile(release: AppRelease): File {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads")
        return File(downloadsDir, release.apkAsset.name)
    }
}
