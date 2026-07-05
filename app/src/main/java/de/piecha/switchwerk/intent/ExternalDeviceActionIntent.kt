package de.piecha.switchwerk.intent

import android.content.Intent

const val RUN_DEVICE_ACTION = "de.piecha.switchwerk.action.RUN_DEVICE_ACTION"
const val RUN_GROUP_ACTION = "de.piecha.switchwerk.action.RUN_GROUP_ACTION"
const val EXTRA_DEVICE_ID = "de.piecha.switchwerk.extra.DEVICE_ID"
const val EXTRA_GROUP_ID = "de.piecha.switchwerk.extra.GROUP_ID"

sealed interface ExternalDeviceActionIntentResult {
    data class Valid(val deviceId: String) : ExternalDeviceActionIntentResult

    data object MissingDeviceId : ExternalDeviceActionIntentResult

    data object InvalidDeviceId : ExternalDeviceActionIntentResult

    data object UnexpectedExtras : ExternalDeviceActionIntentResult
}

sealed interface ExternalSwitchGroupActionIntentResult {
    data class Valid(val groupId: String) : ExternalSwitchGroupActionIntentResult

    data object MissingGroupId : ExternalSwitchGroupActionIntentResult

    data object InvalidGroupId : ExternalSwitchGroupActionIntentResult

    data object UnexpectedExtras : ExternalSwitchGroupActionIntentResult
}

fun Intent.externalDeviceActionRequest(): ExternalDeviceActionIntentResult? {
    if (action != RUN_DEVICE_ACTION) return null
    return parseExternalDeviceActionIntent(
        deviceId = getStringExtra(EXTRA_DEVICE_ID),
        extraKeys = extras?.keySet().orEmpty()
    )
}

fun Intent.externalSwitchGroupActionRequest(): ExternalSwitchGroupActionIntentResult? {
    if (action != RUN_GROUP_ACTION) return null
    return parseExternalSwitchGroupActionIntent(
        groupId = getStringExtra(EXTRA_GROUP_ID),
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
    if (!LOCAL_ID_PATTERN.matches(deviceId)) {
        return ExternalDeviceActionIntentResult.InvalidDeviceId
    }
    return ExternalDeviceActionIntentResult.Valid(deviceId)
}

internal fun parseExternalSwitchGroupActionIntent(
    groupId: String?,
    extraKeys: Set<String>
): ExternalSwitchGroupActionIntentResult {
    if (extraKeys.any { it != EXTRA_GROUP_ID }) {
        return ExternalSwitchGroupActionIntentResult.UnexpectedExtras
    }
    if (groupId == null) return ExternalSwitchGroupActionIntentResult.MissingGroupId
    if (!LOCAL_ID_PATTERN.matches(groupId)) {
        return ExternalSwitchGroupActionIntentResult.InvalidGroupId
    }
    return ExternalSwitchGroupActionIntentResult.Valid(groupId)
}

private val LOCAL_ID_PATTERN = Regex("[A-Za-z0-9._-]{1,128}")
