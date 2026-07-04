package de.piecha.switchwerk.intent

import android.content.Intent

const val RUN_DEVICE_ACTION = "de.piecha.switchwerk.action.RUN_DEVICE_ACTION"
const val EXTRA_DEVICE_ID = "de.piecha.switchwerk.extra.DEVICE_ID"

sealed interface ExternalDeviceActionIntentResult {
    data class Valid(val deviceId: String) : ExternalDeviceActionIntentResult

    data object MissingDeviceId : ExternalDeviceActionIntentResult

    data object InvalidDeviceId : ExternalDeviceActionIntentResult

    data object UnexpectedExtras : ExternalDeviceActionIntentResult
}

fun Intent.externalDeviceActionRequest(): ExternalDeviceActionIntentResult? {
    if (action != RUN_DEVICE_ACTION) return null
    return parseExternalDeviceActionIntent(
        deviceId = getStringExtra(EXTRA_DEVICE_ID),
        extraKeys = extras?.keySet().orEmpty()
    )
}

internal fun parseExternalDeviceActionIntent(
    deviceId: String?,
    extraKeys: Set<String>
): ExternalDeviceActionIntentResult {
    if (extraKeys.any { it != EXTRA_DEVICE_ID }) {
        return ExternalDeviceActionIntentResult.UnexpectedExtras
    }
    if (deviceId == null) return ExternalDeviceActionIntentResult.MissingDeviceId
    if (!DEVICE_ID_PATTERN.matches(deviceId)) {
        return ExternalDeviceActionIntentResult.InvalidDeviceId
    }
    return ExternalDeviceActionIntentResult.Valid(deviceId)
}

private val DEVICE_ID_PATTERN = Regex("[A-Za-z0-9._-]{1,128}")
