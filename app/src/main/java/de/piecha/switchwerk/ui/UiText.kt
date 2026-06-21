package de.piecha.switchwerk.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed interface UiText {
    data class Resource(
        @param:StringRes val resourceId: Int,
        val arguments: List<Any> = emptyList()
    ) : UiText

    data class Dynamic(val value: String) : UiText
}

fun UiText.asString(context: Context): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> context.getString(
        resourceId,
        *arguments.map { argument ->
            if (argument is UiText) argument.asString(context) else argument
        }.toTypedArray()
    )
}

@Composable
fun UiText.asString(): String = asString(LocalContext.current)

fun uiText(@StringRes resourceId: Int, vararg arguments: Any): UiText =
    UiText.Resource(resourceId, arguments.toList())
