package de.piecha.switchwerk.widget

import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.SwitchGroupMember
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetActionTargetTest {

    @Test
    fun availableWidgetActionsExcludeEmptySwitchGroups() {
        val actions = availableWidgetActions(
            devices = listOf(device(id = "device-a", sortOrder = 2)),
            switchGroups = listOf(
                group(id = "group-empty", members = emptyList(), sortOrder = 0),
                group(id = "group-a", members = listOf(member("device-a")), sortOrder = 1)
            )
        )

        assertEquals(
            listOf(
                WidgetActionTarget(WidgetActionTargetType.SWITCH_GROUP, "group-a"),
                WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-a")
            ),
            actions.map { it.target }
        )
    }

    @Test
    fun resolveWidgetActionsUsesCurrentNamesForRenamedTargets() {
        val actions = resolveWidgetActions(
            assignedTargets = listOf(WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-a")),
            devices = listOf(device(id = "device-a", name = "Renamed", actionLabel = "Run")),
            switchGroups = emptyList(),
            maximumEntries = 1
        )

        assertEquals(
            ResolvedWidgetAction.Executable(
                target = WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-a"),
                title = "Renamed",
                subtitle = "Run"
            ),
            actions.single()
        )
    }

    @Test
    fun resolveWidgetActionsMarksDeletedTargetsUnavailable() {
        val actions = resolveWidgetActions(
            assignedTargets = listOf(WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-a")),
            devices = emptyList(),
            switchGroups = emptyList(),
            maximumEntries = 1
        )

        assertEquals(
            ResolvedWidgetAction.Unavailable(
                WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-a")
            ),
            actions.single()
        )
    }

    @Test
    fun resolveWidgetActionsMarksEmptyGroupsUnavailable() {
        val actions = resolveWidgetActions(
            assignedTargets = listOf(WidgetActionTarget(WidgetActionTargetType.SWITCH_GROUP, "group-a")),
            devices = listOf(device(id = "device-a")),
            switchGroups = listOf(group(id = "group-a", members = emptyList())),
            maximumEntries = 1
        )

        assertTrue(actions.single() is ResolvedWidgetAction.Unavailable)
    }

    @Test
    fun resolveWidgetActionsLimitsEntriesToWidgetCapacity() {
        val actions = resolveWidgetActions(
            assignedTargets = listOf(
                WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-a"),
                WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-b")
            ),
            devices = listOf(device(id = "device-a"), device(id = "device-b")),
            switchGroups = emptyList(),
            maximumEntries = 1
        )

        assertEquals(1, actions.size)
        assertEquals(
            WidgetActionTarget(WidgetActionTargetType.DEVICE, "device-a"),
            (actions.single() as ResolvedWidgetAction.Executable).target
        )
    }

    @Test
    fun widgetDimensionsResolveOneByOneCapacity() {
        val dimensions = widgetDimensionsForOptions(
            minWidth = 40,
            minHeight = 40,
            maxWidth = 40,
            maxHeight = 40
        )

        assertEquals(WidgetDimensions(columns = 1, rows = 1), dimensions)
        assertEquals(1, dimensions.maximumEntries)
    }

    @Test
    fun widgetDimensionsResolveOneByTwoCapacity() {
        val dimensions = widgetDimensionsForOptions(
            minWidth = 40,
            minHeight = 110,
            maxWidth = 40,
            maxHeight = 110
        )

        assertEquals(WidgetDimensions(columns = 1, rows = 2), dimensions)
        assertEquals(2, dimensions.maximumEntries)
    }

    @Test
    fun widgetDimensionsUseExpandedMaximumSizeForAdditionalActions() {
        val dimensions = widgetDimensionsForOptions(
            minWidth = 110,
            minHeight = 40,
            maxWidth = 110,
            maxHeight = 110
        )

        assertEquals(WidgetDimensions(columns = 2, rows = 2), dimensions)
        assertEquals(4, dimensions.maximumEntries)
    }

    @Test
    fun oneColumnModeStacksActionsInWideWidget() {
        val configured = WidgetDimensions(columns = 2, rows = 2)
            .withColumnMode(WidgetColumnMode.ONE)

        assertEquals(WidgetDimensions(columns = 1, rows = 2), configured)
        assertEquals(2, configured.maximumEntries)
    }

    @Test
    fun twoColumnModeUsesAllCellsInTallWidget() {
        val configured = WidgetDimensions(columns = 1, rows = 2)
            .withColumnMode(WidgetColumnMode.TWO)

        assertEquals(WidgetDimensions(columns = 2, rows = 2), configured)
        assertEquals(4, configured.maximumEntries)
    }

    @Test
    fun oneByOneWidgetHidesTitle() {
        assertEquals(false, WidgetDimensions(columns = 1, rows = 1).shouldShowTitle("SwitchWerk"))
    }

    @Test
    fun largerWidgetShowsTitle() {
        assertEquals(true, WidgetDimensions(columns = 1, rows = 2).shouldShowTitle("SwitchWerk"))
    }

    @Test
    fun largerWidgetHidesEmptyTitle() {
        assertEquals(false, WidgetDimensions(columns = 2, rows = 2).shouldShowTitle(""))
    }

    private fun device(
        id: String,
        name: String = id,
        actionLabel: String = "Action",
        sortOrder: Int = 0
    ): Device {
        return Device(
            id = id,
            name = name,
            actionLabel = actionLabel,
            apiCall = ApiCall(method = ApiMethod.GET, path = "/relay/0?turn=on"),
            connections = listOf(DeviceConnection(wifiProfileId = "wifi-a", host = "device.local")),
            sortOrder = sortOrder
        )
    }

    private fun group(
        id: String,
        members: List<SwitchGroupMember>,
        sortOrder: Int = 0
    ): SwitchGroup {
        return SwitchGroup(
            id = id,
            name = id,
            actionLabel = "Run",
            sortOrder = sortOrder,
            members = members
        )
    }

    private fun member(deviceId: String): SwitchGroupMember {
        return SwitchGroupMember(
            id = "member-$deviceId",
            deviceId = deviceId,
            pauseAfterMillis = 0,
            sortOrder = 0
        )
    }

}
