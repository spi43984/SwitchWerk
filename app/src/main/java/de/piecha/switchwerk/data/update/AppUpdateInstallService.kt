package de.piecha.switchwerk.data.update

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.net.Uri
import android.os.Build
import android.provider.Settings
import de.piecha.switchwerk.domain.model.AppUpdateError

interface AppUpdateInstallService {
    fun installIntent(apkUri: Uri): Result<Intent>
}

class AndroidAppUpdateInstallService(
    private val context: Context
) : AppUpdateInstallService {
    override fun installIntent(apkUri: Uri): Result<Intent> = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            return@runCatching Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            clipData = ClipData.newUri(context.contentResolver, APK_CLIP_LABEL, apkUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            if (resolveActivity(context.packageManager) == null) {
                throw IllegalStateException(AppUpdateError.Install::class.simpleName)
            }
        }
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val APK_CLIP_LABEL = "SwitchWerk update"
    }
}
