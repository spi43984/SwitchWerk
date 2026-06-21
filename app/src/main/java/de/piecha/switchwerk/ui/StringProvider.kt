package de.piecha.switchwerk.ui

import android.content.Context
import androidx.annotation.StringRes

interface StringProvider {
    fun get(@StringRes resourceId: Int, vararg arguments: Any): String
}

class AndroidStringProvider(private val context: Context) : StringProvider {
    override fun get(resourceId: Int, vararg arguments: Any): String =
        context.getString(resourceId, *arguments)
}
