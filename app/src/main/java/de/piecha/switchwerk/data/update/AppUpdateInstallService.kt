package de.piecha.switchwerk.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.piecha.switchwerk.domain.model.AppUpdateError

interface AppUpdateInstallService {
    fun installIntent(apkUri: Uri): Result<Intent>
}

class AndroidAppUpdateInstallService(
    private val context: Context
) : AppUpdateInstallService {
    override fun installIntent(apkUri: Uri): Result<Intent> = runCatching {
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (resolveActivity(context.packageManager) == null) {
                throw IllegalStateException(AppUpdateError.Install::class.simpleName)
            }
        }
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
