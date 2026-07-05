package de.piecha.switchwerk.widget

import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup

enum class WidgetActionTargetType {
    DEVICE,
    SWITCH_GROUP
}

data class WidgetActionTarget(
    val type: WidgetActionTargetType,
    val id: String
)

data class AvailableWidgetAction(
    val target: WidgetActionTarget,
    val title: String,
    val subtitle: String,
    val sortOrder: Int
)

sealed interface ResolvedWidgetAction {
    data class Executable(
        val target: WidgetActionTarget,
        val title: String,
        val subtitle: String
    ) : ResolvedWidgetAction

    data class Unavailable(
        val target: WidgetActionTarget
    ) : ResolvedWidgetAction
}

fun availableWidgetActions(
    devices: List<Device>,
    switchGroups: List<SwitchGroup>
): List<AvailableWidgetAction> {
    return (
        devices.map { device ->
            AvailableWidgetAction(
                target = WidgetActionTarget(WidgetActionTargetType.DEVICE, device.id),
                title = device.name,
                subtitle = device.actionLabel,
                sortOrder = device.sortOrder
            )
        } +
            switchGroups
                .filter { group -> group.members.isNotEmpty() }
                .map { group ->
                    AvailableWidgetAction(
                        target = WidgetActionTarget(WidgetActionTargetType.SWITCH_GROUP, group.id),
                        title = group.name,
                        subtitle = group.actionLabel,
                        sortOrder = group.sortOrder
                    )
                }
        )
        .sortedWith(compareBy<AvailableWidgetAction> { it.sortOrder }
            .thenBy { it.target.type.name }
            .thenBy { it.target.id })
}

fun resolveWidgetActions(
    assignedTargets: List<WidgetActionTarget>,
    devices: List<Device>,
    switchGroups: List<SwitchGroup>,
    maximumEntries: Int
): List<ResolvedWidgetAction> {
    if (maximumEntries <= 0) return emptyList()
    val devicesById = devices.associateBy { it.id }
    val groupsById = switchGroups.associateBy { it.id }

    return assignedTargets.take(maximumEntries).map { target ->
        when (target.type) {
            WidgetActionTargetType.DEVICE -> {
                val device = devicesById[target.id]
                if (device == null) {
                    ResolvedWidgetAction.Unavailable(target)
                } else {
                    ResolvedWidgetAction.Executable(
                        target = target,
                        title = device.name,
                        subtitle = device.actionLabel
                    )
                }
            }

            WidgetActionTargetType.SWITCH_GROUP -> {
                val group = groupsById[target.id]
                if (group == null || group.members.isEmpty()) {
                    ResolvedWidgetAction.Unavailable(target)
                } else {
                    ResolvedWidgetAction.Executable(
                        target = target,
                        title = group.name,
                        subtitle = group.actionLabel
                    )
                }
            }
        }
    }
}
