package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.local.dao.SwitchGroupDao
import de.piecha.switchwerk.data.local.dao.SwitchGroupMemberDao
import de.piecha.switchwerk.data.local.entity.SwitchGroupEntity
import de.piecha.switchwerk.data.local.entity.SwitchGroupMemberEntity
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.SwitchGroupErrorStrategy
import de.piecha.switchwerk.domain.model.SwitchGroupMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomSwitchGroupRepository(
    private val switchGroupDao: SwitchGroupDao,
    private val switchGroupMemberDao: SwitchGroupMemberDao
) : SwitchGroupRepository {

    override fun observeSwitchGroups(): Flow<List<SwitchGroup>> {
        return combine(
            switchGroupDao.observeAll(),
            switchGroupMemberDao.observeAll()
        ) { groups, members ->
            groups.map { group ->
                group.toDomain(
                    members = members
                        .filter { it.groupId == group.id }
                        .sortedBy { it.sortOrder }
                )
            }
        }
    }

    override suspend fun getSwitchGroups(): List<SwitchGroup> {
        return switchGroupDao.getAll().map { group ->
            group.toDomain(switchGroupMemberDao.getForGroup(group.id))
        }
    }

    override suspend fun saveSwitchGroup(group: SwitchGroup) {
        switchGroupDao.upsert(group.toEntity())
        switchGroupMemberDao.deleteForGroup(group.id)
        switchGroupMemberDao.upsertAll(
            group.members.mapIndexed { index, member ->
                member.toEntity(groupId = group.id, sortOrder = index)
            }
        )
    }

    override suspend fun updateSwitchGroupOrder(groupIds: List<String>) {
        val existingGroups = switchGroupDao.getAll()
        val existingGroupIds = existingGroups.map { it.id }.toSet()
        val orderedGroupIds = groupIds.filter { it in existingGroupIds } +
            existingGroups.map { it.id }.filterNot { it in groupIds }

        orderedGroupIds.forEachIndexed { index, groupId ->
            switchGroupDao.updateSortOrder(groupId, index)
        }
    }

    override suspend fun updateSwitchGroupSortOrders(sortOrders: Map<String, Int>) {
        sortOrders.forEach { (groupId, sortOrder) ->
            switchGroupDao.updateSortOrder(groupId, sortOrder)
        }
    }

    override suspend fun deleteSwitchGroup(groupId: String) {
        switchGroupMemberDao.deleteForGroup(groupId)
        switchGroupDao.deleteById(groupId)
    }

    private fun SwitchGroupEntity.toDomain(
        members: List<SwitchGroupMemberEntity>
    ): SwitchGroup {
        return SwitchGroup(
            id = id,
            name = name,
            actionLabel = actionLabel,
            sortOrder = sortOrder,
            errorStrategy = SwitchGroupErrorStrategy.valueOf(errorStrategy),
            members = members.map { it.toDomain() }
        )
    }

    private fun SwitchGroup.toEntity(): SwitchGroupEntity {
        return SwitchGroupEntity(
            id = id,
            name = name,
            actionLabel = actionLabel,
            sortOrder = sortOrder,
            errorStrategy = errorStrategy.name
        )
    }

    private fun SwitchGroupMemberEntity.toDomain(): SwitchGroupMember {
        return SwitchGroupMember(
            id = id,
            deviceId = deviceId,
            pauseAfterMillis = pauseAfterMillis,
            sortOrder = sortOrder
        )
    }

    private fun SwitchGroupMember.toEntity(
        groupId: String,
        sortOrder: Int
    ): SwitchGroupMemberEntity {
        return SwitchGroupMemberEntity(
            id = id,
            groupId = groupId,
            deviceId = deviceId,
            sortOrder = sortOrder,
            pauseAfterMillis = pauseAfterMillis
        )
    }
}
