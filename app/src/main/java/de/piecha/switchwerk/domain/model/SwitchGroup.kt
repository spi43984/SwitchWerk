package de.piecha.switchwerk.domain.model

data class SwitchGroup(
    val id: String,
    val name: String,
    val actionLabel: String,
    val sortOrder: Int,
    val shortcutEnabled: Boolean = false,
    val color: DeviceColor = DeviceColor.NONE,
    val errorStrategy: SwitchGroupErrorStrategy = SwitchGroupErrorStrategy.ABORT_ON_ERROR,
    val members: List<SwitchGroupMember>
)

data class SwitchGroupMember(
    val id: String,
    val deviceId: String,
    val pauseAfterMillis: Long,
    val sortOrder: Int
)

enum class SwitchGroupErrorStrategy {
    ABORT_ON_ERROR,
    CONTINUE_ON_ERROR
}
